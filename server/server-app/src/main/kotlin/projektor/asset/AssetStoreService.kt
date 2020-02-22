package projektor.asset

import java.io.InputStream
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.server.api.PublicId

class AssetStoreService(private val config: AssetStoreConfig) {
    private val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(config.url, config.accessKey, config.secretKey))

    fun conditionallyCreateBucketIfNotExists() {
        if (config.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(config.bucketName)
        }
    }

    fun addAsset(publicId: PublicId, assetName: String, assetStream: InputStream) {
        objectStoreClient.putObject(config.bucketName, assetName, assetStream)
    }
}
