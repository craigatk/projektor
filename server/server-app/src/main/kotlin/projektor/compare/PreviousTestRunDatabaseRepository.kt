package projektor.compare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.CODE_COVERAGE_RUN
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.TEST_RUN
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withBranchType
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType
import java.time.LocalDateTime
import java.time.ZoneOffset

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

    override suspend fun findMostRecentRunWithCoverage(branchType: BranchType, repoName: String, projectName: String?): RecentTestRun? =
        withContext(Dispatchers.IO) {
            val recentTestRun = dslContext.select(TEST_RUN.PUBLIC_ID, TEST_RUN.CREATED_TIMESTAMP)
                .from(TEST_RUN)
                .innerJoin(GIT_METADATA).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                .where(
                    GIT_METADATA.REPO_NAME.eq(repoName)
                        .and(withBranchType(branchType))
                        .let {
                            if (projectName == null)
                                it.and(GIT_METADATA.PROJECT_NAME.isNull)
                            else
                                it.and(GIT_METADATA.PROJECT_NAME.eq(projectName))
                        }
                )
                .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc().nullsLast())
                .limit(1)
                .fetchOne()

            recentTestRun?.let {
                RecentTestRun(
                    publicId = PublicId(it.component1()),
                    createdTimestamp = it.component2().toInstant(ZoneOffset.UTC)
                )
            }
        }

    data class CurrentRunInfo(val repoName: String, val createdTimestamp: LocalDateTime, val projectName: String?)
}
