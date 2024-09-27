package com.despread.snapshothelper.model

data class SnapshotMeta(
    val timestamp: Long,
    val version: String,
    val fileSize: Long,
)