package projektor.persistence

import io.ktor.server.config.MapApplicationConfig
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class PersistenceConfigTest {
    @Test
    fun `should default to database when persistence type not set`() {
        val persistenceConfig = PersistenceConfig.createPersistenceConfig(MapApplicationConfig())

        expectThat(persistenceConfig.type).isEqualTo(PersistenceType.DATABASE)
    }

    @Test
    fun `should parse database persistence type`() {
        val applicationConfig = MapApplicationConfig("ktor.persistence.type" to "Database")

        val persistenceConfig = PersistenceConfig.createPersistenceConfig(applicationConfig)

        expectThat(persistenceConfig.type).isEqualTo(PersistenceType.DATABASE)
    }

    @Test
    fun `should fail on unsupported persistence type`() {
        val applicationConfig = MapApplicationConfig("ktor.persistence.type" to "carrier-pigeon")

        expectThrows<IllegalArgumentException> { PersistenceConfig.createPersistenceConfig(applicationConfig) }
    }

    @Test
    fun `should parse object-store persistence config`() {
        val applicationConfig =
            MapApplicationConfig(
                "ktor.persistence.type" to "object-store",
                "ktor.persistence.objectStore.url" to "http://localhost:9000",
                "ktor.persistence.objectStore.bucketName" to "projektor",
                "ktor.persistence.objectStore.accessKey" to "access",
                "ktor.persistence.objectStore.secretKey" to "secret",
                "ktor.persistence.objectStore.keyPrefix" to "prod",
                "ktor.persistence.objectStore.autoCreateBucket" to "true",
            )

        expectThat(PersistenceConfig.createPersistenceConfig(applicationConfig).type).isEqualTo(PersistenceType.OBJECT_STORE)
        expectThat(ObjectStorePersistenceConfig.createObjectStorePersistenceConfig(applicationConfig))
            .isEqualTo(ObjectStorePersistenceConfig("http://localhost:9000", "projektor", "access", "secret", "prod", true))
    }

    @Test
    fun `should default object-store key prefix and bucket creation`() {
        val applicationConfig =
            MapApplicationConfig(
                "ktor.persistence.objectStore.url" to "http://localhost:9000",
                "ktor.persistence.objectStore.bucketName" to "projektor",
                "ktor.persistence.objectStore.accessKey" to "access",
                "ktor.persistence.objectStore.secretKey" to "secret",
            )

        expectThat(ObjectStorePersistenceConfig.createObjectStorePersistenceConfig(applicationConfig))
            .isEqualTo(ObjectStorePersistenceConfig("http://localhost:9000", "projektor", "access", "secret", "", false))
    }

    @Test
    fun `should fail when required object-store config is missing`() {
        val applicationConfig = MapApplicationConfig("ktor.persistence.objectStore.url" to "http://localhost:9000")

        expectThrows<IllegalArgumentException> { ObjectStorePersistenceConfig.createObjectStorePersistenceConfig(applicationConfig) }
    }
}
