package projektor.organization.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL.firstValue
import org.jooq.impl.DSL.partitionBy
import projektor.database.generated.Tables.*
import projektor.server.api.PublicId
import projektor.util.toInstantInDefaultZone

class OrganizationCoverageDatabaseRepository(private val dslContext: DSLContext) : OrganizationCoverageRepository {
    override suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
        withContext(Dispatchers.IO) {
            // Every column that varies per run has to come from the most recent run, otherwise each
            // older run adds its own row to the DISTINCT results
            val mostRecentRunFirst =
                partitionBy(GIT_METADATA.REPO_NAME, GIT_METADATA.PROJECT_NAME)
                    .orderBy(TEST_RUN.CREATED_TIMESTAMP.desc(), TEST_RUN.ID.desc())

            val publicId = firstValue(TEST_RUN.PUBLIC_ID).over(mostRecentRunFirst).`as`("public_id")
            val branchName = firstValue(GIT_METADATA.BRANCH_NAME).over(mostRecentRunFirst).`as`("branch_name")
            val createdTimestamp = firstValue(TEST_RUN.CREATED_TIMESTAMP).over(mostRecentRunFirst).`as`("created_timestamp")

            dslContext.selectDistinct(publicId, GIT_METADATA.REPO_NAME, GIT_METADATA.PROJECT_NAME, branchName, createdTimestamp)
                .from(GIT_METADATA)
                .innerJoin(TEST_RUN).on(GIT_METADATA.TEST_RUN_ID.eq(TEST_RUN.ID))
                .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_STATS.ID.eq(CODE_COVERAGE_GROUP.STATS_ID))
                .where(
                    GIT_METADATA.ORG_NAME.eq(orgName)
                        .and(GIT_METADATA.IS_MAIN_BRANCH.eq(true))
                        .and(CODE_COVERAGE_STATS.LINE_COVERED.isNotNull),
                )
                .fetch { record ->
                    RepositoryTestRun(
                        repoName = record.get(GIT_METADATA.REPO_NAME),
                        publicId = PublicId(record.get(publicId)),
                        createdTimestamp = record.get(createdTimestamp).toInstantInDefaultZone(),
                        projectName = record.get(GIT_METADATA.PROJECT_NAME),
                        branchName = record.get(branchName),
                    )
                }
        }
}
