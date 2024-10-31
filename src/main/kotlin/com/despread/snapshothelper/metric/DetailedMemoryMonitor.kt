package com.despread.snapshothelper.metric

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory

@Component
class DetailedMemoryMonitor {
    private val logger: KLogger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 300000) // Every 5m
    fun monitorDetailedMemory() {
        val runtime = Runtime.getRuntime()
        val memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans()

        logger.info { """
            Detailed Memory Status:
            Total Memory: ${runtime.totalMemory() / 1024 / 1024}MB
            Free Memory: ${runtime.freeMemory() / 1024 / 1024}MB
            Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024}MB
            Max Memory: ${runtime.maxMemory() / 1024 / 1024}MB
            
            Memory Pools:
            ${memoryPoolMXBeans.joinToString("\n") { pool ->
            "${pool.name}: ${pool.usage.used / 1024 / 1024}MB / ${pool.usage.max / 1024 / 1024}MB"
        }}
        """.trimIndent()}
    }
}