package projektor.objectstore

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.RemoveObjectsArgs
import io.minio.errors.ErrorResponseException
import io.minio.messages.DeleteObject
import projektor.objectstore.bucket.BucketCreationException
import java.io.InputStream

class ObjectStoreClient(config: ObjectStoreConfig) {
    private val minioClient = MinioClient.builder()
        .endpoint(config.url)
        .credentials(config.accessKey, config.secretKey)
        .build()

    fun createBucketIfNotExists(bucketName: String) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
            }
        } catch (e: ErrorResponseException) {
            throw BucketCreationException("Error creating bucket", e)
        }
    }

    fun bucketExists(bucketName: String) = minioClient.bucketExists(
        BucketExistsArgs.builder().bucket(bucketName).build()
    )

    fun putObject(bucketName: String, objectName: String, stream: InputStream) {
        minioClient.putObject(
            PutObjectArgs
                .builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(stream, -1, 10485760)
                .contentType("application/octet-stream")
                .build()
        )
    }

    fun getObject(bucketName: String, objectName: String): InputStream? =
        try {
            minioClient.getObject(
                GetObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
        } catch (e: ErrorResponseException) {
            null
        }

    fun removeObject(bucketName: String, objectName: String) {
        minioClient.removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
    }

    fun removeObjects(bucketName: String, objectNames: List<String>) {
        val objects = objectNames.map { DeleteObject(it) }
        minioClient.removeObjects(
            RemoveObjectsArgs
                .builder()
                .bucket(bucketName)
                .objects(objects)
                .build()
        )
    }
}
