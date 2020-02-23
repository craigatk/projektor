package projektor.attachment

import io.ktor.util.KtorExperimentalAPI
import java.io.InputStream
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.server.api.PublicId

@KtorExperimentalAPI
class AttachmentStoreService(private val config: AttachmentStoreConfig) {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

    fun conditionallyCreateBucketIfNotExists() {
        if (config.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(config.bucketName)
        }
    }

    fun addAttachment(publicId: PublicId, assetName: String, assetStream: InputStream) {
        objectStoreClient.putObject(config.bucketName, attachmentKey(publicId, assetName), assetStream)
    }

    fun getAttachment(publicId: PublicId, assetName: String) =
        objectStoreClient.getObject(config.bucketName, attachmentKey(publicId, assetName))

    companion object {
        fun attachmentKey(publicId: PublicId, assetName: String) = "${publicId.id}-$assetName"
    }
}
