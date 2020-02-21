package projektor.objectstore

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener

class ObjectCleanupListener(
    private val objectStoreClient: ObjectStoreClient,
    private val bucketName: String,
    private val objectName: String
) : TestListener {
    override fun beforeTest(testCase: TestCase) {
        objectStoreClient.createBucketIfNotExists(bucketName)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        objectStoreClient.removeObject(bucketName, objectName)
    }
}
