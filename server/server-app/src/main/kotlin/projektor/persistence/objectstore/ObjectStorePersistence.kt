package projektor.persistence.objectstore

import org.koin.core.module.Module
import projektor.attachment.AttachmentObjectStoreRepository
import projektor.attachment.AttachmentRepository
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.ObjectStorePersistenceConfig
import projektor.persistence.Persistence

class ObjectStorePersistence(store: JsonObjectStore) : Persistence {
    override val koinModule: Module = createObjectStorePersistenceModule(store)

    override val attachmentRepository: AttachmentRepository = AttachmentObjectStoreRepository(store)

    // The object store client doesn't hold any resources that need closing
    override fun close() {}

    companion object {
        fun create(config: ObjectStorePersistenceConfig): ObjectStorePersistence {
            val client = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

            if (config.autoCreateBucket) {
                client.createBucketIfNotExists(config.bucketName)
            } else if (!client.bucketExists(config.bucketName)) {
                throw IllegalStateException(
                    "Object store bucket ${config.bucketName} does not exist, create it or set PERSISTENCE_OBJECT_STORE_AUTO_CREATE_BUCKET",
                )
            }

            return ObjectStorePersistence(JsonObjectStore(client, config.bucketName, config.keyPrefix))
        }
    }
}
