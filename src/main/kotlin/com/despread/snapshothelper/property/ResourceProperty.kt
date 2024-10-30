package com.despread.snapshothelper.property

import lombok.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ToString
@Configuration
@ConfigurationProperties(prefix = "resource")
class ResourceProperty {
    companion object {
        const val DEFAULT_BUFFER_SIZE_IN_BYTE: Long = 16L * 1024 * 1024 // 16MB
        const val DEFAULT_MULTIPART_SIZE_IN_BYTE: Long = 100L * 1024 * 1024 // 100MB
    }

    var bufferSizeInByte: Long = DEFAULT_BUFFER_SIZE_IN_BYTE
    var multipartSizeInByte: Long = DEFAULT_BUFFER_SIZE_IN_BYTE
}