package projektor.persistence

import io.ktor.server.config.ApplicationConfig
import io.micrometer.core.instrument.MeterRegistry
import org.koin.core.module.Module
import projektor.attachment.AttachmentRepository
import projektor.database.DataSourceConfig
import java.io.Closeable

/**
 * A storage backend for the server's data. Provides the repository implementations (plus anything else
 * that's tied to the backend, like the scheduler lock) as a Koin module.
 */
interface Persistence : Closeable {
    val koinModule: Module

    // Needed before Koin starts so the attachment service can be created during application startup
    val attachmentRepository: AttachmentRepository

    companion object {
        fun createPersistence(
            applicationConfig: ApplicationConfig,
            metricRegistry: MeterRegistry,
        ): Persistence =
            when (PersistenceConfig.createPersistenceConfig(applicationConfig).type) {
                PersistenceType.DATABASE ->
                    DatabasePersistence.create(DataSourceConfig.createDataSourceConfig(applicationConfig), metricRegistry)
            }
    }
}
