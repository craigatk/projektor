package projektor.persistence

import io.ktor.server.config.ApplicationConfig

enum class PersistenceType(val configValue: String) {
    DATABASE("database"),
    OBJECT_STORE("object-store"),
    ;

    companion object {
        fun fromConfigValue(configValue: String): PersistenceType =
            entries.find { it.configValue.equals(configValue, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Unsupported persistence type '$configValue', supported types are ${entries.map { it.configValue }}",
                )
    }
}

data class PersistenceConfig(val type: PersistenceType) {
    companion object {
        fun createPersistenceConfig(applicationConfig: ApplicationConfig) =
            PersistenceConfig(
                applicationConfig.propertyOrNull("ktor.persistence.type")?.getString()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { PersistenceType.fromConfigValue(it) }
                    ?: PersistenceType.DATABASE,
            )
    }
}

data class ObjectStorePersistenceConfig(
    val url: String,
    val bucketName: String,
    val accessKey: String,
    val secretKey: String,
    // Lets the persistence data share a bucket with other data, such as attachments
    val keyPrefix: String,
    val autoCreateBucket: Boolean,
) {
    companion object {
        fun createObjectStorePersistenceConfig(applicationConfig: ApplicationConfig): ObjectStorePersistenceConfig {
            fun requiredProperty(name: String) =
                applicationConfig.propertyOrNull("ktor.persistence.objectStore.$name")?.getString()?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("ktor.persistence.objectStore.$name must be set when using object-store persistence")

            return ObjectStorePersistenceConfig(
                url = requiredProperty("url"),
                bucketName = requiredProperty("bucketName"),
                accessKey = requiredProperty("accessKey"),
                secretKey = requiredProperty("secretKey"),
                keyPrefix = applicationConfig.propertyOrNull("ktor.persistence.objectStore.keyPrefix")?.getString() ?: "",
                autoCreateBucket =
                    applicationConfig.propertyOrNull(
                        "ktor.persistence.objectStore.autoCreateBucket",
                    )?.getString()?.toBoolean() ?: false,
            )
        }
    }
}
