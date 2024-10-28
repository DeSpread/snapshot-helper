package com.despread.snapshothelper.util

import com.despread.snapshothelper.property.ResourceProperty
import com.despread.snapshothelper.service.AwsS3Service
import com.despread.snapshothelper.service.SlackService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.jpountz.lz4.LZ4FrameOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executor

@Service
class CompressorService(
    private val resourceProperty: ResourceProperty,
    private val awsS3Service: AwsS3Service,
    private val compressorTaskExecutor: Executor,
    private val slackService: SlackService
) {
    suspend fun compressToTarLz4AndUploadToS3(
        sourceDir: Path,
        s3Key: String
    ) = coroutineScope {
        val bufferSize = resourceProperty.bufferSizeInByte.toInt()
        val totalBytes: Long = FileUtil.getDirectorySize(sourceDir.toFile())

        val compressBuffer = ByteArray(bufferSize)
        val channel = Channel<ByteArray>(Channel.BUFFERED)

        val compressionJob = launch(Dispatchers.IO) {
            try {
                BufferUtil.channelOutputStream(channel, bufferSize).use { outputStream ->
                    compressDirectoryToTarLz4(sourceDir, outputStream, compressBuffer)
                }
            } catch (e: Exception) {
                channel.close(e)
                throw e
            } finally {
                channel.close()
            }
        }

        val uploadJob = launch(Dispatchers.IO) {
            try {
                BufferUtil.channelInputStream(channel).use { inputStream ->
                    awsS3Service.uploadToS3WithMultipart(
                        inputStream,
                        s3Key,
                        bufferSize.toLong(),
                        totalBytes
                    )
                }
            } catch (e: Exception) {
                compressionJob.cancel()
                throw e
            }
        }

        joinAll(compressionJob, uploadJob)
        slackService.sendMessage(message = "File uploaded to S3 Successfully. s3Key: $s3Key")
    }

    private suspend fun compressDirectoryToTarLz4(
        sourceDir: Path,
        outputStream: OutputStream,
        buffer: ByteArray
    ) = withContext(compressorTaskExecutor.asCoroutineDispatcher()) {
        // Use single buffer layer not multiple
        LZ4FrameOutputStream(outputStream).use { lz4Out ->
            TarArchiveOutputStream(lz4Out).use { tarOut ->
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
                Files.walk(sourceDir).use { paths ->
                    paths.forEach { path ->
                        processFile(path, sourceDir, tarOut, buffer)
                    }
                }
                tarOut.finish()
            }
        }
    }

    private fun processFile(
        path: Path,
        sourceDir: Path,
        tarOut: TarArchiveOutputStream,
        buffer: ByteArray
    ) {
        val entryName = sourceDir.relativize(path).toString()
        val tarEntry = TarArchiveEntry(path.toFile(), entryName)
        tarOut.putArchiveEntry(tarEntry)

        if (!Files.isDirectory(path)) {
            Files.newInputStream(path).use { input ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    tarOut.write(buffer, 0, bytesRead)
                }
            }
        }
        tarOut.closeArchiveEntry()
    }
}