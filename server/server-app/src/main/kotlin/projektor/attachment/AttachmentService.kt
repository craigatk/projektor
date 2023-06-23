package projektor.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.bucket.BucketCreationException
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment
import java.io.InputStream
import java.math.BigDecimal

class AttachmentService(
    private val config: AttachmentConfig,
    private val attachmentRepository: AttachmentRepository
) {
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

    fun attachmentSizeValid(attachmentSizeInBytes: Long?): Boolean {
        val maxSizeInBytes = config.maxSizeMB?.let { it * BigDecimal.valueOf(1024) * BigDecimal.valueOf(1024) }

        return maxSizeInBytes == null ||
            attachmentSizeInBytes == null ||
            attachmentSizeInBytes.toBigDecimal() <= maxSizeInBytes
    }

    suspend fun addAttachment(publicId: PublicId, fileName: String, attachmentStream: InputStream, attachmentSize: Long?): AddAttachmentResult {
        val objectName = attachmentObjectName(publicId, fileName)

        return try {
            withContext(Dispatchers.IO) {
                objectStoreClient.putObject(config.bucketName, objectName, attachmentStream)
            }

            attachmentRepository.addAttachment(publicId, Attachment(fileName = fileName, objectName = objectName, fileSize = attachmentSize))

            AddAttachmentResult.Success
        } catch (e: Exception) {
            logger.error("Error saving attachment '$fileName' for test run ${publicId.id}", e)
            AddAttachmentResult.Failure(e.message)
        }
    }

    suspend fun getAttachment(publicId: PublicId, attachmentFileName: String) = withContext(Dispatchers.IO) {
        objectStoreClient.getObject(config.bucketName, attachmentObjectName(publicId, attachmentFileName))
    }

    suspend fun listAttachments(publicId: PublicId): List<Attachment> = attachmentRepository.listAttachments(publicId)

    suspend fun deleteAttachments(publicId: PublicId) {
        val attachments = listAttachments(publicId)

        attachments.forEach { attachment ->
            objectStoreClient.removeObject(config.bucketName, attachment.objectName)

            attachmentRepository.deleteAttachment(publicId, attachment.objectName)
        }
    }

    companion object {
        fun attachmentObjectName(publicId: PublicId, attachmentFileName: String) = "${publicId.id}-$attachmentFileName"
    }
}
