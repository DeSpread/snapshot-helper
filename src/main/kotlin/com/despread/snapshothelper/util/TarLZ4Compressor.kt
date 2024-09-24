package com.despread.snapshothelper.util

import net.jpountz.lz4.LZ4FrameOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.nio.file.Path

object TarLZ4Compressor {
    fun compressDirectoryToTarLz4(sourceDir: Path, outputTarLz4: Path) {
        // Create tar file enclose en entire directory
        Files.newOutputStream(outputTarLz4).use { fileOut ->
            BufferedOutputStream(fileOut).use { buffOut ->
                LZ4FrameOutputStream(buffOut).use { lz4Out ->
                    TarArchiveOutputStream(lz4Out).use { tarOut ->
                        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)

                        // Tar all directory
                        Files.walk(sourceDir).forEach { path ->
                            // Kep relative path
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
    }
}