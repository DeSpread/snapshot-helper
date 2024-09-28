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
    private val compressorTaskExecutor: Executor
) {

    private suspend fun compressDirectoryToTarLz4(
        sourceDir: Path, outputStream: OutputStream
    ) = withContext(compressorTaskExecutor.asCoroutineDispatcher()) {
        BufferedOutputStream(outputStream, 6 * 1024 * 1024).use { buffOut ->
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
                                    fileInputStream.copyTo(tarOut, 6 * 1024 * 1024)
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
        val pipedInputStream = PipedInputStream(6 * 1024 * 1024)
        val pipedOutputStream = PipedOutputStream(pipedInputStream)

        val compressionJob = launch {
            compressDirectoryToTarLz4(sourceDir, pipedOutputStream)
            pipedOutputStream.close()
        }

        val uploadJob = launch {
            awsS3Service.uploadToS3WithMultipart(pipedInputStream, s3Key)
        }

        joinAll(compressionJob, uploadJob)
    }
}
