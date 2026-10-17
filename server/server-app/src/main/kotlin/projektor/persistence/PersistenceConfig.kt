package projektor.persistence

import io.ktor.server.config.ApplicationConfig

enum class PersistenceType(val configValue: String) {
    DATABASE("database"),
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
