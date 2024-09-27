package com.despread.snapshothelper.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig {

    @Bean(name = ["compressorTaskExecutor"])
    fun compressorTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("CompressorTask-")
        executor.initialize()
        return executor
    }

    @Bean(name = ["s3UploadTaskExecutor"])
    fun s3UploadTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 6
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("s3UploadTask-")
        executor.initialize()
        return executor
    }
}