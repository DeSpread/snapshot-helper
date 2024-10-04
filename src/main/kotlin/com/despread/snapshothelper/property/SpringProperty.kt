package com.despread.snapshothelper.property

import lombok.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ToString
@Configuration
@ConfigurationProperties(prefix = "spring")
class SpringProperty {
    lateinit var application: Application

    class Application {
        lateinit var name: String
    }
}