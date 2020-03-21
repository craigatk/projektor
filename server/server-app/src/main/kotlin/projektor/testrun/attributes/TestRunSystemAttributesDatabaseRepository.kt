package projektor.testrun.attributes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import projektor.database.generated.Tables.TEST_RUN_SYSTEM_ATTRIBUTES
import projektor.server.api.PublicId
import projektor.server.api.attributes.TestRunSystemAttributes

class TestRunSystemAttributesDatabaseRepository(private val dslContext: DSLContext) : TestRunSystemAttributesRepository {

    override suspend fun fetchAttributes(publicId: PublicId): TestRunSystemAttributes? =
            withContext(Dispatchers.IO) {
                dslContext
                        .select(TEST_RUN_SYSTEM_ATTRIBUTES.fields().toList())
                        .from(TEST_RUN_SYSTEM_ATTRIBUTES)
                        .where(TEST_RUN_SYSTEM_ATTRIBUTES.TEST_RUN_PUBLIC_ID.eq(publicId.id))
                        .fetchOneInto(TestRunSystemAttributes::class.java)
            }

    override suspend fun pin(publicId: PublicId) = upsertPinned(publicId, true)

    override suspend fun unpin(publicId: PublicId) = upsertPinned(publicId, false)

    private suspend fun upsertPinned(publicId: PublicId, newPinnedValue: Boolean): Int =
        withContext(Dispatchers.IO) {
            try {
                dslContext
                        .insertInto(TEST_RUN_SYSTEM_ATTRIBUTES, TEST_RUN_SYSTEM_ATTRIBUTES.TEST_RUN_PUBLIC_ID, TEST_RUN_SYSTEM_ATTRIBUTES.PINNED)
                        .values(publicId.id, newPinnedValue)
                        .onDuplicateKeyUpdate()
                        .set(TEST_RUN_SYSTEM_ATTRIBUTES.PINNED, newPinnedValue)
                        .execute()
            } catch (e: DataAccessException) {
                0
            }
        }
}
