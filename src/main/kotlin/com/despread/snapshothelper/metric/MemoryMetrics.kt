package com.despread.snapshothelper.metric

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemoryMetrics {
    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @PostConstruct
    fun init() {
        Gauge.builder("jvm.memory.used") { getUsedMemory() }
            .description("Used memory in bytes")
            .register(meterRegistry)
    }

    private fun getUsedMemory(): Double {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()).toDouble()
    }
}