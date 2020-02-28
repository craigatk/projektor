package projektor.attachment

import io.ktor.util.KtorExperimentalAPI
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.bucket.BucketCreationException
import projektor.server.api.Attachment
import projektor.server.api.PublicId

@KtorExperimentalAPI
class AttachmentService(private val config: AttachmentStoreConfig, private val attachmentRepository: AttachmentRepository) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

    fun conditionallyCreateBucketIfNotExists() {
        if (config.autoCreateBucket) {
            try {
                objectStoreClient.createBucketIfNotExists(config.bucketName)
            } catch (e: BucketCreationException) {
                logger.error("Error creating bucket ${config.bucketName}", e)
            }
        }
    }

    suspend fun addAttachment(publicId: PublicId, fileName: String, attachmentStream: InputStream) {
        val objectName = attachmentObjectName(publicId, fileName)

        withContext(Dispatchers.IO) {
            objectStoreClient.putObject(config.bucketName, objectName, attachmentStream)
        }

        attachmentRepository.addAttachment(publicId, Attachment(fileName = fileName, objectName = objectName, fileSize = null))
    }

    suspend fun getAttachment(publicId: PublicId, attachmentFileName: String) = withContext(Dispatchers.IO) {
        objectStoreClient.getObject(config.bucketName, attachmentObjectName(publicId, attachmentFileName))
    }

    suspend fun listAttachments(publicId: PublicId): List<Attachment> = withContext(Dispatchers.IO) {
        attachmentRepository.listAttachments(publicId)
    }

    companion object {
        fun attachmentObjectName(publicId: PublicId, attachmentFileName: String) = "${publicId.id}-$attachmentFileName"
    }
}
