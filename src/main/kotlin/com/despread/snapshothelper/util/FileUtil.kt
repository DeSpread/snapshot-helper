package com.despread.snapshothelper.util

import java.io.File

object FileUtil {
    fun getDirectorySize(directory: File): Long {
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("${directory.absolutePath} is not a valid directory.")
        }

        return directory.walk()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    fun formatSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var sizeInUnits = size.toDouble()
        var unitIndex = 0
        while (sizeInUnits >= 1024 && unitIndex < units.size - 1) {
            sizeInUnits /= 1024
            unitIndex++
        }
        return String.format("%.2f %s", sizeInUnits, units[unitIndex])
    }
}
