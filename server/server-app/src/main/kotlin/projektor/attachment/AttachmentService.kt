package projektor.attachment

import io.ktor.util.KtorExperimentalAPI
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.server.api.PublicId

@KtorExperimentalAPI
class AttachmentService(private val config: AttachmentStoreConfig, private val attachmentRepository: AttachmentRepository) {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

    fun conditionallyCreateBucketIfNotExists() {
        if (config.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(config.bucketName)
        }
    }

    suspend fun addAttachment(publicId: PublicId, fileName: String, attachmentStream: InputStream) {
        val objectName = attachmentKey(publicId, fileName)

        withContext(Dispatchers.IO) {
            objectStoreClient.putObject(config.bucketName, objectName, attachmentStream)
        }

        attachmentRepository.addAttachment(publicId, Attachment(fileName = fileName, objectName = objectName, fileSize = null))
    }

    suspend fun getAttachment(publicId: PublicId, attachmentFileName: String) = withContext(Dispatchers.IO) {
        objectStoreClient.getObject(config.bucketName, attachmentKey(publicId, attachmentFileName))
    }

    suspend fun listAttachments(publicId: PublicId): List<Attachment> = withContext(Dispatchers.IO) {
        attachmentRepository.listAttachments(publicId)
    }

    companion object {
        fun attachmentKey(publicId: PublicId, attachmentFileName: String) = "${publicId.id}-$attachmentFileName"
    }
}
