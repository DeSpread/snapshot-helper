package com.despread.snapshothelper.property

import lombok.ToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ToString
@Configuration
@ConfigurationProperties(prefix = "cloud.aws")
class AwsClientProperty {

    lateinit var s3: S3
    lateinit var credentials: Credentials

    class S3 {
        lateinit var bucketName: String
        lateinit var region: String
    }

    class Credentials {
        lateinit var accessKey: String
        lateinit var secretKey: String
    }
}