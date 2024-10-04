package com.despread.snapshothelper.property

import lombok.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ToString
@Configuration
@ConfigurationProperties(prefix = "resource")
class ResourceProperty {
    companion object {
        const val DEFAULT_BUFFER_SIZE_IN_BYTE: Long = 6 * 1024 * 1024
    }

    var bufferSizeInByte: Long = DEFAULT_BUFFER_SIZE_IN_BYTE
}