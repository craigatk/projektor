package projektor.objectstore

import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult

class ObjectCleanupListener(
    private val objectStoreClient: ObjectStoreClient,
    private val bucketName: String,
    private val objectName: String,
) : TestListener {
    override suspend fun beforeTest(testCase: TestCase) {
        objectStoreClient.createBucketIfNotExists(bucketName)
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        objectStoreClient.removeObject(bucketName, objectName)
    }
}
