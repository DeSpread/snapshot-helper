package com.despread.snapshothelper

import com.despread.snapshothelper.property.AwsClientProperty
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AwsClientProperty::class)
class SnapshotHelperApplication

fun main(args: Array<String>) {
    val dotenv = Dotenv.configure() // Fetching dotnev
        .filename(".env")
        .load()

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<SnapshotHelperApplication>(*args)
}
