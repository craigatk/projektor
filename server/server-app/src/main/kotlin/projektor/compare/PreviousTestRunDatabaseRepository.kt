package projektor.compare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId

class PreviousTestRunDatabaseRepository(private val dslContext: DSLContext) : PreviousTestRunRepository {
    override suspend fun findPreviousMainBranchRun(publicId: PublicId): PublicId? =
            withContext(Dispatchers.IO) {
                val repoName = dslContext.select(GIT_METADATA.REPO_NAME)
                        .from(GIT_METADATA)
                        .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                        .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                        .limit(1)
                        .fetchOne(GIT_METADATA.REPO_NAME)

                if (repoName != null) {
                    val publicId = dslContext.select(TEST_RUN.PUBLIC_ID)
                            .from(TEST_RUN)
                            .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                            .where(GIT_METADATA.IS_MAIN_BRANCH.eq(true)
                                    .and(TEST_RUN.PUBLIC_ID.ne(publicId.id))
                                    .and(GIT_METADATA.REPO_NAME.eq(repoName)))
                            .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc().nullsLast())
                            .limit(1)
                            .fetchOne(TEST_RUN.PUBLIC_ID)

                    publicId?.let { PublicId(it) }
                } else {
                    null
                }
            }
}
