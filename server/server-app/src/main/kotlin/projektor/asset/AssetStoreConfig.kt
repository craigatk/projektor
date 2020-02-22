package projektor.asset

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
data class AssetStoreConfig(
    val url: String,
    val bucketName: String,
    val autoCreateBucket: Boolean,
    val accessKey: String,
    val secretKey: String
) {
    companion object {
        fun assetStoreEnabled(applicationConfig: ApplicationConfig): Boolean =
                applicationConfig.propertyOrNull("ktor.assetStore.enabled")?.getString()?.toBoolean() ?: false

        fun createAssetConfig(applicationConfig: ApplicationConfig) = AssetStoreConfig(
                applicationConfig.property("ktor.assetStore.url").getString(),
                applicationConfig.property("ktor.assetStore.bucketName").getString(),
                applicationConfig.propertyOrNull("ktor.assetStore.autoCreateBucket")?.getString()?.toBoolean() ?: false,
                applicationConfig.property("ktor.assetStore.accessKey").getString(),
                applicationConfig.property("ktor.assetStore.secretKey").getString()
        )
    }
}
