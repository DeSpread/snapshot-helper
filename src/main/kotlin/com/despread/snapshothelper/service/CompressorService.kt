package com.despread.snapshothelper.util

import com.despread.snapshothelper.property.ResourceProperty
import com.despread.snapshothelper.service.AwsS3Service
import com.despread.snapshothelper.service.SlackService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.jpountz.lz4.LZ4FrameOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executor

@Service
class CompressorService(
    private val resourceProperty: ResourceProperty,
    private val awsS3Service: AwsS3Service,
    @Qualifier("compressorTaskExecutor") private val compressorTaskExecutor: Executor,
    @Qualifier("s3UploadTaskExecutor") private val s3UploadTaskExecutor: Executor,
    private val slackService: SlackService
) {
    suspend fun compressToTarLz4AndUploadToS3(
        sourceDir: Path,
        s3Key: String,
        notifyProgressIntervalSecond: Long
    ) = coroutineScope {
        val bufferSizeInByte = resourceProperty.bufferSizeInByte.toInt()
        val multipartSizeInByte = resourceProperty.multipartSizeInByte.toInt()
        val totalBytes: Long = FileUtil.getDirectorySize(sourceDir.toFile())

        val compressBuffer = ByteArray(bufferSizeInByte)
        val channel = Channel<ByteArray>(Channel.BUFFERED)

        val compressionJob = launch(compressorTaskExecutor.asCoroutineDispatcher()) {
            try {
                BufferUtil.channelOutputStream(channel, bufferSizeInByte).use { outputStream ->
                    compressDirectoryToTarLz4(sourceDir, outputStream, compressBuffer)
                }
            } catch (e: Exception) {
                channel.close(e)
                throw e
            } finally {
                channel.close()
            }
        }

        val uploadJob = launch(s3UploadTaskExecutor.asCoroutineDispatcher()) {
            try {
                BufferUtil.channelInputStream(channel).use { inputStream ->
                    awsS3Service.uploadToS3WithMultipart(
                        inputStream,
                        s3Key,
                        multipartSizeInByte.toLong(),
                        totalBytes,
                        progressCallback = { bytesUploaded ->
                            if (bytesUploaded > 0L) {
                                slackService.sendMessage(message = "Upload progress: $bytesUploaded bytes completed.")
                            }
                        },
                        notifyProgressIntervalSecond = notifyProgressIntervalSecond
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