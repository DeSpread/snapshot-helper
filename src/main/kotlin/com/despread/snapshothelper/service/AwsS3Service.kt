package com.despread.snapshothelper.service

import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.model.ObjectMetadata
import com.despread.snapshothelper.config.AwsClientProperty
import com.despread.snapshothelper.config.S3ClientConfig
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.io.InputStream


@Service
class AwsS3Service(
    private val s3ClientConfig: S3ClientConfig,
    private val awsClientProperty: AwsClientProperty
) {
    private val logger: KLogger = KotlinLogging.logger {}

    private val bucketName: String = awsClientProperty.s3.bucketName

    fun uploadFromInputStreamToS3(
        inputStream: InputStream,
        contentLength: Long = 0L,
        s3Key: String
    ): String {
        logger.debug { awsClientProperty.toString() }

        val objectMetadata = ObjectMetadata()
        if (inputStream.available() > 0) {
            objectMetadata.contentLength = contentLength
        }

        return try {
            s3ClientConfig.s3Client().putObject(bucketName, s3Key, inputStream, objectMetadata)
            s3ClientConfig.s3Client().getUrl(bucketName, s3Key).toString()
        } catch (e: AmazonClientException) {
            logger.error { "Upload failed from input stream to s3: ${e.message}" }
            throw e
        }
    }
}