package projektor.attachment

import io.ktor.config.ApplicationConfig
import java.math.BigDecimal

data class AttachmentConfig(
    val url: String,
    val bucketName: String,
    val autoCreateBucket: Boolean,
    val accessKey: String,
    val secretKey: String,
    val maxSizeMB: BigDecimal?
) {
    companion object {
        fun attachmentsEnabled(applicationConfig: ApplicationConfig): Boolean =
            applicationConfig.propertyOrNull("ktor.attachment.url")?.getString()?.isNotEmpty() ?: false

        fun createAttachmentConfig(applicationConfig: ApplicationConfig) = AttachmentConfig(
            applicationConfig.property("ktor.attachment.url").getString(),
            applicationConfig.property("ktor.attachment.bucketName").getString(),
            applicationConfig.propertyOrNull("ktor.attachment.autoCreateBucket")?.getString()?.toBoolean() ?: false,
            applicationConfig.property("ktor.attachment.accessKey").getString(),
            applicationConfig.property("ktor.attachment.secretKey").getString(),
            applicationConfig.propertyOrNull("ktor.attachment.maxSizeMB")?.getString()?.toBigDecimal()
        )
    }
}
