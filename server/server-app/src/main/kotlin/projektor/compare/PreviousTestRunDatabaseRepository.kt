package projektor.compare

import java.sql.Timestamp
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.*
import projektor.server.api.PublicId

class PreviousTestRunDatabaseRepository(private val dslContext: DSLContext) : PreviousTestRunRepository {
    override suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
            withContext(Dispatchers.IO) {
                val currentRunInfo = dslContext.select(GIT_METADATA.REPO_NAME, TEST_RUN.CREATED_TIMESTAMP)
                        .from(GIT_METADATA)
                        .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                        .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                        .limit(1)
                        .fetchOneInto(CurrentRunInfo::class.java)

                if (currentRunInfo != null) {
                    val previousPublicId = dslContext.select(TEST_RUN.PUBLIC_ID)
                            .from(TEST_RUN)
                            .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                            .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                            .where(GIT_METADATA.IS_MAIN_BRANCH.eq(true)
                                    .and(TEST_RUN.PUBLIC_ID.ne(publicId.id))
                                    .and(GIT_METADATA.REPO_NAME.eq(currentRunInfo.repoName))
                                    .and(TEST_RUN.CREATED_TIMESTAMP.lessThan(Timestamp.valueOf(currentRunInfo.createdTimestamp)))
                            )
                            .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                            .limit(1)
                            .fetchOne(TEST_RUN.PUBLIC_ID)

                    previousPublicId?.let { PublicId(it) }
                } else {
                    null
                }
            }

    data class CurrentRunInfo(val repoName: String, val createdTimestamp: LocalDateTime)
}
