package com.despread.snapshothelper.service

import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.util.CompressorService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path


@Service
class SnapshotService(
    private val compressorService: CompressorService,
    private val slackService: SlackService
) {
    private val logger: KLogger = KotlinLogging.logger {}

    suspend fun snapshot(snapshotDto: SnapshotDto) {
        logger.debug { "snapshotDto: $snapshotDto" }

        runCatching {
            slackService.sendMessage(message = "Starting the snapshot. thread: ${Thread.currentThread().name}, dto: $snapshotDto")

            validate(snapshotDto.sourceDirectoryPath)
            compressorService.compressToTarLz4AndUploadToS3(
                sourceDir = Path.of(snapshotDto.sourceDirectoryPath),
                s3Key = snapshotDto.s3Key
            )
        }.onFailure {
            slackService.sendMessage(message = it.message)
        }
    }

    private fun validate(sourceDirectoryPath: String) {
        logger.info { "source directory: $sourceDirectoryPath" }

        require(File(sourceDirectoryPath).exists()) {
            "Source directory does not exist: $sourceDirectoryPath"
        }
    }
}