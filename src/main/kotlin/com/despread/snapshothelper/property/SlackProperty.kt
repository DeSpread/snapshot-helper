package com.despread.snapshothelper.property

import lombok.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ToString
@Configuration
@ConfigurationProperties(prefix = "notifier.slack")
class SlackProperty {
    lateinit var channelId: String
    lateinit var botToken: String
}