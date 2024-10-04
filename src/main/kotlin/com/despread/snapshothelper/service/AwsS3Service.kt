package com.despread.snapshothelper.service

import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.model.*
import com.despread.snapshothelper.config.S3ClientConfig
import com.despread.snapshothelper.property.AwsClientProperty
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.PipedInputStream
import java.util.concurrent.Executor


@Service
class AwsS3Service(
    private val s3ClientConfig: S3ClientConfig,
    private val awsClientProperty: AwsClientProperty,
    private val s3UploadTaskExecutor: Executor
) {
    private val logger: KLogger = KotlinLogging.logger {}
    private val bucketName: String = awsClientProperty.s3.bucketName

    suspend fun uploadToS3WithMultipart(
        pipedInputStream: PipedInputStream,
        s3Key: String,
        partSizeInByte: Long
    ) = withContext(s3UploadTaskExecutor.asCoroutineDispatcher()) {
        val multipartUploadRequest = InitiateMultipartUploadRequest(bucketName, s3Key)
        val initResponse = s3ClientConfig.s3Client().initiateMultipartUpload(multipartUploadRequest)
        val uploadId = initResponse.uploadId

        val partETags = mutableListOf<PartETag>()
        var partNumber = 1

        try {
            var bytesRead: Int
            val buffer = ByteArray(partSizeInByte.toInt())

            while (pipedInputStream.read(buffer).also { bytesRead = it } != -1) {
                if (bytesRead < partSizeInByte && pipedInputStream.available() > 0) {
                    continue
                }

                val inputStreamForPart = buffer.inputStream(0, bytesRead)

                val uploadPartRequest = UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(s3Key)
                    .withUploadId(uploadId)
                    .withPartNumber(partNumber)
                    .withInputStream(inputStreamForPart)
                    .withPartSize(bytesRead.toLong())

                val uploadPartResult = s3ClientConfig.s3Client().uploadPart(uploadPartRequest)
                partETags.add(uploadPartResult.partETag)

                partNumber++
            }

            val completeMultipartUploadRequest =
                CompleteMultipartUploadRequest(bucketName, s3Key, uploadId, partETags)
            s3ClientConfig.s3Client().completeMultipartUpload(completeMultipartUploadRequest)
            s3ClientConfig.s3Client().setObjectAcl(bucketName, s3Key, CannedAccessControlList.PublicRead)

            logger.info { "Successful to multipart upload with public access. s3Key: $s3Key" }
        } catch (e: Exception) {
            logger.error { "Something went wrong while uploading. uploadId: $uploadId" }
            s3ClientConfig.s3Client()
                .abortMultipartUpload(AbortMultipartUploadRequest(bucketName, s3Key, uploadId))
            throw e
        } finally {
            pipedInputStream.close()
        }
    }

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
        } finally {
            inputStream.close()
        }
    }
}