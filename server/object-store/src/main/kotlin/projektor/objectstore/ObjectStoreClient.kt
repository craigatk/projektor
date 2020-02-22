package projektor.objectstore

import io.minio.MinioClient
import java.io.InputStream

class ObjectStoreClient(private val config: ObjectStoreConfig) {
    private val minioClient = MinioClient(config.url, config.accessKey, config.secretKey)

    fun createBucketIfNotExists(bucketName: String) {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(bucketName)
        }
    }

    fun deleteBucket(bucketName: String) {
        minioClient.deleteBucketLifeCycle(bucketName)
    }

    fun bucketExists(bucketName: String) = minioClient.bucketExists(bucketName)

    fun putObject(bucketName: String, objectName: String, localFilePath: String) {
        minioClient.putObject(bucketName, objectName, localFilePath)
    }

    fun putObject(bucketName: String, objectName: String, stream: InputStream) {
        minioClient.putObject(bucketName, objectName, stream, "application/octet-stream")
    }

    fun getObject(bucketName: String, objectName: String): InputStream =
            minioClient.getObject(bucketName, objectName)

    fun removeObject(bucketName: String, objectName: String) {
        minioClient.removeObject(bucketName, objectName)
    }

    fun removeObjects(bucketName: String, objectNames: List<String>) {
        minioClient.removeObjects(bucketName, objectNames)
    }
}
