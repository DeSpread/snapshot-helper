package com.despread.snapshothelper.util

import com.despread.snapshothelper.service.AwsS3Service
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
    private val awsS3Service: AwsS3Service,
) {
    private suspend fun compressDirectoryToTarLz4(
        compressorTaskExecutor: Executor,
        sourceDir: Path, outputStream: OutputStream
    ) = withContext(compressorTaskExecutor.asCoroutineDispatcher()) {
            BufferedOutputStream(outputStream).use { buffOut ->
                LZ4FrameOutputStream(buffOut).use { lz4Out ->
                    TarArchiveOutputStream(lz4Out).use { tarOut ->
                        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)

                        // Tar the entire directory
                        Files.walk(sourceDir).forEach { path ->
                            val entryName = sourceDir.relativize(path).toString()
                            val tarEntry = TarArchiveEntry(path.toFile(), entryName)

                            tarOut.putArchiveEntry(tarEntry)
                            if (!Files.isDirectory(path)) {
                                Files.copy(path, tarOut)
                            }
                            tarOut.closeArchiveEntry()
                        }

                        tarOut.finish()
                    }
                }
            }
        }

    private suspend fun uploadToS3(
        s3UploadTaskExecutor: Executor,
        pipedInputStream: PipedInputStream,
        s3Key: String
    ) = withContext(s3UploadTaskExecutor.asCoroutineDispatcher()) {
            awsS3Service.uploadFromInputStreamToS3(
                inputStream = pipedInputStream,
                s3Key = s3Key
            )
            pipedInputStream.close()
        }

    suspend fun compressToTarLz4AndUploadToS3(
        s3UploadTaskExecutor: Executor,
        compressorTaskExecutor: Executor,
        sourceDir: Path, s3Key: String
    ) = coroutineScope {
        val pipedInputStream = PipedInputStream()
        val pipedOutputStream = PipedOutputStream(pipedInputStream)

        val compressionJob = launch {
            compressDirectoryToTarLz4(compressorTaskExecutor, sourceDir, pipedOutputStream)
            pipedOutputStream.close()
        }

        val uploadJob = launch {
            uploadToS3(s3UploadTaskExecutor, pipedInputStream, s3Key)
        }

        joinAll(compressionJob, uploadJob)
    }
}
