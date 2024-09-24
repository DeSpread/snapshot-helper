package com.despread.snapshothelper.service

import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.util.TarLZ4Compressor
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


@Service
class AptosSnapshotService(
    private val logger: KLogger = KotlinLogging.logger {}
) {

    fun snapshot(snapshotDto: SnapshotDto) {
        logger.debug { "snapshotDto: $snapshotDto" }
        compressToTarLz4File("/Users/cryptogang/lz_test", "/Users/cryptogang/lz_test_compressed/lz_test.tar.lz4")
        createMetafile("/Users/cryptogang/lz_test_compressed/lz_test.tar.lz4")
        uploadFile()
    }

    private fun compressToTarLz4File(sourceDirectoryPath: String, targetFilePath: String) {
        try {
            logger.info { "source directory: $sourceDirectoryPath" }
            logger.info { "target file: $targetFilePath" }

            if (!File(sourceDirectoryPath).exists()) {
                throw IllegalArgumentException("Source directory does not exist: $sourceDirectoryPath")
            }

            if (File(targetFilePath).exists()) {
                File(targetFilePath).delete()
                logger.info { "Delete target file: $targetFilePath" }
            }

            TarLZ4Compressor.compressDirectoryToTarLz4(Path.of(sourceDirectoryPath), Path.of(targetFilePath))

            logger.info { "Compression successful" }
        } catch (e: Exception) {
            throw RuntimeException("Compression failed.", e)
        }
    }

    private fun createMetafile(filePath: String) {
        val fileSize = Files.size(Path.of(filePath))

        logger.info { "file size: $fileSize" }
    }

    private fun uploadFile() {

    }
}