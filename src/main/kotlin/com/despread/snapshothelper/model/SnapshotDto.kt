package com.despread.snapshothelper.model

data class SnapshotDto(
    val sourceDirectoryPath: String,
    val targetFilePath: String,
    val fileName: String,
)