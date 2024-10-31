package com.despread.snapshothelper.metric

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory

@Component
class GCMonitor {
    private val logger: KLogger = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()

        gcBeans.forEach { gc ->
            logger.info { """
                GC Name: ${gc.name}
                Collection Count: ${gc.collectionCount}
                Collection Time: ${gc.collectionTime}ms
            """.trimIndent()}
        }
    }
}