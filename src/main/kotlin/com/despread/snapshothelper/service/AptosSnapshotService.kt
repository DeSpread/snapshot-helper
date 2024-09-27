package com.despread.snapshothelper.service

import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.util.CompressorService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executor


@Service
class AptosSnapshotService(
    private val s3UploadTaskExecutor: Executor,
    private val compressorTaskExecutor: Executor,
    private val compressorService: CompressorService,
) {
    private val logger: KLogger = KotlinLogging.logger {}

    suspend fun snapshot(snapshotDto: SnapshotDto) {
        logger.debug { "snapshotDto: $snapshotDto" }
        validate(snapshotDto.sourceDirectoryPath)
        compressToTarLz4AndUploadToS3(Path.of(snapshotDto.sourceDirectoryPath), snapshotDto.s3Key)
    }

    private suspend fun compressToTarLz4AndUploadToS3(sourceDirectoryPath: Path, s3Key: String) {
        compressorService.compressToTarLz4AndUploadToS3(
            s3UploadTaskExecutor = s3UploadTaskExecutor,
            compressorTaskExecutor = compressorTaskExecutor,
            sourceDir = sourceDirectoryPath,
            s3Key = s3Key
        )
    }

    private fun validate(sourceDirectoryPath: String) {
        logger.info { "source directory: $sourceDirectoryPath" }

        require(File(sourceDirectoryPath).exists()) {
            "Source directory does not exist: $sourceDirectoryPath"
        }
    }
}