package com.despread.snapshothelper.util

import com.despread.snapshothelper.property.ResourceProperty
import com.despread.snapshothelper.service.AwsS3Service
import com.despread.snapshothelper.service.SlackService
import kotlinx.coroutines.*
import net.jpountz.lz4.LZ4FrameOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.springframework.stereotype.Service
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
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

    private suspend fun compressDirectoryToTarLz4(
        sourceDir: Path, outputStream: OutputStream
    ) = withContext(compressorTaskExecutor.asCoroutineDispatcher()) {
        BufferedOutputStream(outputStream, resourceProperty.bufferSizeInByte.toInt()).use { buffOut ->
            LZ4FrameOutputStream(buffOut).use { lz4Out ->
                TarArchiveOutputStream(lz4Out).use { tarOut ->
                    tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                    tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)

                    Files.walk(sourceDir).use { paths ->
                        paths.forEach { path ->
                            val entryName = sourceDir.relativize(path).toString()
                            val tarEntry = TarArchiveEntry(path.toFile(), entryName)

                            tarOut.putArchiveEntry(tarEntry)

                            if (!Files.isDirectory(path)) {
                                Files.newInputStream(path).use { fileInputStream ->
                                    fileInputStream.copyTo(tarOut, resourceProperty.bufferSizeInByte.toInt())
                                }
                            }

                            tarOut.closeArchiveEntry()
                        }
                    }

                    tarOut.finish()
                }
            }
        }
    }

    suspend fun compressToTarLz4AndUploadToS3(
        sourceDir: Path, s3Key: String
    ) = coroutineScope {
        val pipedInputStream = PipedInputStream(resourceProperty.bufferSizeInByte.toInt())
        val pipedOutputStream = PipedOutputStream(pipedInputStream)

        val compressionJob = launch (Dispatchers.IO) {
            compressDirectoryToTarLz4(sourceDir, pipedOutputStream)
            pipedOutputStream.close()
            slackService.sendMessage(message = "Successfully compressed")
        }

        val uploadJob = launch (Dispatchers.IO) {
            awsS3Service.uploadToS3WithMultipart(pipedInputStream, s3Key, resourceProperty.bufferSizeInByte)
            slackService.sendMessage(message = "Successfully uploaded to S3")
        }

        joinAll(compressionJob, uploadJob)
    }
}