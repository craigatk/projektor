package projektor.attachment

import java.io.InputStream
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.server.api.PublicId

class AttachmentStoreService(private val config: AttachmentStoreConfig) {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

    fun conditionallyCreateBucketIfNotExists() {
        if (config.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(config.bucketName)
        }
    }

    fun addAttachment(publicId: PublicId, assetName: String, assetStream: InputStream) {
        objectStoreClient.putObject(config.bucketName, assetName, assetStream)
    }
}
