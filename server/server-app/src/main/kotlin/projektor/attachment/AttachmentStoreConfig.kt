package projektor.attachment

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class AttachmentStoreConfig(
    val url: String,
    val bucketName: String,
    val autoCreateBucket: Boolean,
    val accessKey: String,
    val secretKey: String
) {
    companion object {
        fun attachmentStoreEnabled(applicationConfig: ApplicationConfig): Boolean =
                applicationConfig.propertyOrNull("ktor.attachment.url")?.getString()?.isNotEmpty() ?: false

        fun createAttachmentStoreConfig(applicationConfig: ApplicationConfig) = AttachmentStoreConfig(
                applicationConfig.property("ktor.attachment.url").getString(),
                applicationConfig.property("ktor.attachment.bucketName").getString(),
                applicationConfig.propertyOrNull("ktor.attachment.autoCreateBucket")?.getString()?.toBoolean() ?: false,
                applicationConfig.property("ktor.attachment.accessKey").getString(),
                applicationConfig.property("ktor.attachment.secretKey").getString()
        )
    }
}
