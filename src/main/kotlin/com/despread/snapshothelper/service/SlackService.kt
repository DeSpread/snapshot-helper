package com.despread.snapshothelper.service

import com.despread.snapshothelper.exntends.TracingContext
import com.despread.snapshothelper.property.SlackProperty
import com.despread.snapshothelper.property.SpringProperty
import com.slack.api.Slack
import com.slack.api.methods.response.conversations.ConversationsListResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.coroutines.coroutineContext

@Service
class SlackService(
    private val springProperty: SpringProperty,
    private val slackProperty: SlackProperty
) {
    private val logger: KLogger = KotlinLogging.logger {}

    suspend fun sendMessage(channelId: String = slackProperty.channelId, message: String?) {
        val client = Slack.getInstance().methods()
        val span = coroutineContext[TracingContext]?.span!!
        val traceId = span.context().traceId()

        logger.info { "[${traceId}] : $message" }
        runCatching {
            client.chatPostMessage {
                it.token(slackProperty.botToken)
                    .channel(channelId)
                    .text("[${springProperty.application.name}][${traceId}] : $message")
            }
        }.onFailure { e ->
            logger.error { "[${traceId}] Slack Send Error: {$e.message}" }
        }
    }

    suspend fun showSlackInfo() {
        val client = Slack.getInstance().methods()
        var result: ConversationsListResponse = ConversationsListResponse()

        runCatching {
            result = client.conversationsList {
                it.token(slackProperty.botToken)
            }
            logger.info { result.toString() }
        }.onSuccess {
            result.channels.stream().forEach {
                logger.info { "${it.name} -> ${it.id}" }
            }
        }
    }
}