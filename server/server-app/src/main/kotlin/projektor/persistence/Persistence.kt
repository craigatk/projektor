package projektor.persistence

import io.ktor.server.config.ApplicationConfig
import io.micrometer.core.instrument.MeterRegistry
import org.koin.core.module.Module
import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentRepository
import projektor.database.DataSourceConfig
import projektor.persistence.objectstore.ObjectStorePersistence
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
        private val logger = LoggerFactory.getLogger(Persistence::class.java)

        fun createPersistence(
            applicationConfig: ApplicationConfig,
            metricRegistry: MeterRegistry,
        ): Persistence {
            val persistenceConfig = PersistenceConfig.createPersistenceConfig(applicationConfig)
            val instrumentation =
                PersistenceInstrumentation(persistenceConfig.type, metricRegistry, persistenceConfig.slowCallThreshold)

            logger.info(
                "Using {} persistence, logging repository calls slower than {} ms",
                persistenceConfig.type.configValue,
                persistenceConfig.slowCallThreshold.toMillis(),
            )

            return when (persistenceConfig.type) {
                PersistenceType.DATABASE ->
                    DatabasePersistence.create(DataSourceConfig.createDataSourceConfig(applicationConfig), metricRegistry, instrumentation)
                PersistenceType.OBJECT_STORE ->
                    ObjectStorePersistence.create(
                        ObjectStorePersistenceConfig.createObjectStorePersistenceConfig(applicationConfig),
                        metricRegistry,
                        instrumentation,
                    )
            }
        }
    }
}
