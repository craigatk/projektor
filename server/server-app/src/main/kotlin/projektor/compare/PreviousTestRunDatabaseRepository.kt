package projektor.compare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.*
import projektor.server.api.PublicId
import java.time.LocalDateTime

class PreviousTestRunDatabaseRepository(private val dslContext: DSLContext) : PreviousTestRunRepository {
    override suspend fun findPreviousMainBranchRunWithCoverage(publicId: PublicId): PublicId? =
        withContext(Dispatchers.IO) {
            val currentRunInfo = dslContext.select(
                GIT_METADATA.REPO_NAME,
                TEST_RUN.CREATED_TIMESTAMP,
                GIT_METADATA.PROJECT_NAME
            )
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
                    .where(
                        GIT_METADATA.IS_MAIN_BRANCH.eq(true)
                            .and(TEST_RUN.PUBLIC_ID.ne(publicId.id))
                            .and(GIT_METADATA.REPO_NAME.eq(currentRunInfo.repoName))
                            .let {
                                if (currentRunInfo.projectName == null)
                                    it.and(GIT_METADATA.PROJECT_NAME.isNull)
                                else
                                    it.and(GIT_METADATA.PROJECT_NAME.eq(currentRunInfo.projectName))
                            }
                            .and(TEST_RUN.CREATED_TIMESTAMP.lessThan(currentRunInfo.createdTimestamp))
                    )
                    .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                    .limit(1)
                    .fetchOne(TEST_RUN.PUBLIC_ID)

                previousPublicId?.let { PublicId(it) }
            } else {
                null
            }
        }

    override suspend fun findMostRecentMainBranchRunWithCoverage(repoName: String, projectName: String?): PublicId? =
        withContext(Dispatchers.IO) {
            val previousPublicId = dslContext.select(TEST_RUN.PUBLIC_ID)
                .from(TEST_RUN)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                .where(
                    GIT_METADATA.IS_MAIN_BRANCH.eq(true)
                        .and(GIT_METADATA.REPO_NAME.eq(repoName))
                        .let {
                            if (projectName == null)
                                it.and(GIT_METADATA.PROJECT_NAME.isNull)
                            else
                                it.and(GIT_METADATA.PROJECT_NAME.eq(projectName))
                        }
                )
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                .limit(1)
                .fetchOne(TEST_RUN.PUBLIC_ID)

            previousPublicId?.let { PublicId(it) }
        }

    data class CurrentRunInfo(val repoName: String, val createdTimestamp: LocalDateTime, val projectName: String?)
}
