package com.despread.snapshothelper.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class S3ClientConfig(private val awsClientProperty: AwsClientProperty) {

    @Bean
    fun s3Client(): AmazonS3Client {
        return AmazonS3ClientBuilder.standard()
            .withRegion(Regions.fromName(awsClientProperty.s3.region))
            .withCredentials(
                AWSStaticCredentialsProvider(
                    BasicAWSCredentials(
                        awsClientProperty.credentials.accessKey,
                        awsClientProperty.credentials.secretKey
                    )
                )
            )
            .build() as AmazonS3Client
    }
}