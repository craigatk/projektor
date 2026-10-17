package projektor.testrun.attributes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.SystemAttributesDocument
import projektor.persistence.objectstore.TestRunDocumentStore
import projektor.server.api.PublicId
import projektor.server.api.attributes.TestRunSystemAttributes

class TestRunSystemAttributesObjectStoreRepository(private val documentStore: TestRunDocumentStore) : TestRunSystemAttributesRepository {
    private val store = documentStore.store

    override suspend fun fetchAttributes(publicId: PublicId): TestRunSystemAttributes? =
        withContext(Dispatchers.IO) {
            store.read<SystemAttributesDocument>(ObjectStoreKeys.systemAttributes(publicId))?.let { TestRunSystemAttributes(it.pinned) }
        }

    override suspend fun pin(publicId: PublicId) = writePinned(publicId, true)

    override suspend fun unpin(publicId: PublicId) = writePinned(publicId, false)

    // Returns the number of test runs updated, matching the database repository where the attributes
    // reference the test run so can't be saved for a test run that doesn't exist.
    private suspend fun writePinned(
        publicId: PublicId,
        newPinnedValue: Boolean,
    ): Int =
        if (documentStore.testRunExists(publicId)) {
            withContext(Dispatchers.IO) {
                store.write(ObjectStoreKeys.systemAttributes(publicId), SystemAttributesDocument(newPinnedValue))
            }
            1
        } else {
            0
        }
}
