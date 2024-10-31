package com.despread.snapshothelper.model

data class SnapshotDto(
    val sourceDirectoryPath: String,
    val s3Key: String,
    val notifyProgressIntervalSecond:Long = 60
)