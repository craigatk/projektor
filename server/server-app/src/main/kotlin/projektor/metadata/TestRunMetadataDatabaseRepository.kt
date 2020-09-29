package projektor.metadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.*
import projektor.server.api.PublicId
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.metadata.TestRunMetadata

class TestRunMetadataDatabaseRepository(private val dslContext: DSLContext) : TestRunMetadataRepository {
    override suspend fun fetchGitMetadata(publicId: PublicId): TestRunGitMetadata? =
        withContext(Dispatchers.IO) {
            dslContext.select(GIT_METADATA.fields().toList())
                .from(GIT_METADATA)
                .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .fetchOneInto(TestRunGitMetadata::class.java)
        }

    override suspend fun fetchResultsMetadata(publicId: PublicId): TestRunMetadata? =
        withContext(Dispatchers.IO) {
            dslContext.select(RESULTS_METADATA.fields().toList())
                .from(RESULTS_METADATA)
                .innerJoin(TEST_RUN).on(RESULTS_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .fetchOneInto(TestRunMetadata::class.java)
        }
}
