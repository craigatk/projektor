package projektor.persistence.objectstore

import io.micrometer.core.instrument.MeterRegistry
import org.koin.core.module.Module
import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentObjectStoreRepository
import projektor.attachment.AttachmentRepository
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.ObjectStoreTelemetry
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.ObjectStorePersistenceConfig
import projektor.persistence.Persistence
import projektor.persistence.PersistenceInstrumentation
import projektor.schedule.Scheduler

class ObjectStorePersistence(
    store: JsonObjectStore,
    instrumentation: PersistenceInstrumentation,
) : Persistence {
    private val testRunIndex = TestRunIndex(TestRunDocumentStore(store))

    override val koinModule: Module = createObjectStorePersistenceModule(store, instrumentation, testRunIndex)

    override val attachmentRepository: AttachmentRepository =
        instrumentation.instrument<AttachmentRepository>(
            AttachmentObjectStoreRepository(store),
        )

    override fun scheduleJobs(scheduler: Scheduler) {
        IndexRepairScheduledJob.scheduleIndexRepairJob(testRunIndex, scheduler)
    }

    // The object store client doesn't hold any resources that need closing
    override fun close() {}

    companion object {
        private val logger = LoggerFactory.getLogger(ObjectStorePersistence::class.java)

        fun create(
            config: ObjectStorePersistenceConfig,
            metricRegistry: MeterRegistry,
            instrumentation: PersistenceInstrumentation,
        ): ObjectStorePersistence {
            logger.info(
                "Object store persistence using bucket {} at {} with key prefix '{}'",
                config.bucketName,
                config.url,
                config.keyPrefix,
            )

            val client =
                ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey), ObjectStoreTelemetry(metricRegistry))

            if (config.autoCreateBucket) {
                client.createBucketIfNotExists(config.bucketName)
            } else if (!client.bucketExists(config.bucketName)) {
                throw IllegalStateException(
                    "Object store bucket ${config.bucketName} does not exist, create it or set PERSISTENCE_OBJECT_STORE_AUTO_CREATE_BUCKET",
                )
            }

            val store = JsonObjectStore(client, config.bucketName, config.keyPrefix)

            // Fail at startup rather than silently losing concurrent updates on a store that ignores write conditions
            store.verifyConditionalWrites()

            return ObjectStorePersistence(store, instrumentation)
        }
    }
}
