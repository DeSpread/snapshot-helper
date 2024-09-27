package com.despread.snapshothelper

import com.despread.snapshothelper.config.AwsClientProperty
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AwsClientProperty::class)
class SnapshotHelperApplication

fun main(args: Array<String>) {
    runApplication<SnapshotHelperApplication>(*args)
}
