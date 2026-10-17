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
}
