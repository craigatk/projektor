package projektor.repository.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.coverage.toCoverageStats
import projektor.database.generated.Tables.CODE_COVERAGE_RUN
import projektor.database.generated.Tables.CODE_COVERAGE_STATS
import projektor.database.generated.Tables.GIT_METADATA
import projektor.database.generated.Tables.REPOSITORY_CURRENT_COVERAGE
import projektor.database.generated.Tables.TEST_RUN
import projektor.parser.coverage.model.CoverageReportStats
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withBranchType
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository.Companion.withProjectName
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.coverage.RepositoryCoverageTimelineEntry
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.streams.toList

class RepositoryCoverageDatabaseRepository(private val dslContext: DSLContext) : RepositoryCoverageRepository {
    private val repositoryCoverageTimelineEntryMapper =
        JdbcMapperFactory.newInstance()
            .addKeys("public_id")
            .ignorePropertyNotFound()
            .newMapper(ReportTimelineEntry::class.java)

    override suspend fun fetchRepositoryCoverageTimeline(
        branchType: BranchType,
        repoName: String,
        projectName: String?,
    ): RepositoryCoverageTimeline? =
        withContext(Dispatchers.IO) {
            val resultSet =
                dslContext.select(
                    TEST_RUN.PUBLIC_ID,
                    TEST_RUN.CREATED_TIMESTAMP,
                    DSL.sum(CODE_COVERAGE_STATS.BRANCH_COVERED).`as`("coverage_stats_branch_stat_covered"),
                    DSL.sum(CODE_COVERAGE_STATS.BRANCH_MISSED).`as`("coverage_stats_branch_stat_missed"),
                    DSL.sum(CODE_COVERAGE_STATS.STATEMENT_COVERED).`as`("coverage_stats_statement_stat_covered"),
                    DSL.sum(CODE_COVERAGE_STATS.STATEMENT_MISSED).`as`("coverage_stats_statement_stat_missed"),
                    DSL.sum(CODE_COVERAGE_STATS.LINE_COVERED).`as`("coverage_stats_line_stat_covered"),
                    DSL.sum(CODE_COVERAGE_STATS.LINE_MISSED).`as`("coverage_stats_line_stat_missed"),
                )
                    .from(GIT_METADATA)
                    .innerJoin(TEST_RUN).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                    .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                    .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_STATS.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                    .where(
                        GIT_METADATA.REPO_NAME.eq(repoName)
                            .and(withBranchType(branchType))
                            .and(withProjectName(projectName))
                            .and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")),
                    )
                    .groupBy(TEST_RUN.PUBLIC_ID, TEST_RUN.CREATED_TIMESTAMP)
                    .orderBy(TEST_RUN.CREATED_TIMESTAMP.asc())
                    .fetchResultSet()

            val timelineEntries: List<ReportTimelineEntry> =
                resultSet.use {
                    repositoryCoverageTimelineEntryMapper.stream(resultSet).toList()
                }

            if (timelineEntries.isNotEmpty()) RepositoryCoverageTimeline(timelineEntries.map { it.toFullEntry() }) else null
        }

    override suspend fun coverageExists(
        repoName: String,
        projectName: String?,
    ): Boolean =
        withContext(Dispatchers.IO) {
            dslContext.fetchExists(
                dslContext.select(CODE_COVERAGE_RUN.ID)
                    .from(GIT_METADATA)
                    .innerJoin(TEST_RUN).on(TEST_RUN.ID.eq(GIT_METADATA.TEST_RUN_ID))
                    .innerJoin(CODE_COVERAGE_RUN).on(TEST_RUN.PUBLIC_ID.eq(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID))
                    .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_STATS.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                    .where(
                        GIT_METADATA.REPO_NAME.eq(repoName)
                            .and(withProjectName(projectName))
                            .and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")),
                    ),
            )
        }

    override suspend fun saveLastKnownCoverageIfNewer(
        repoName: String,
        projectName: String?,
        branchName: String?,
        coveredPercentage: BigDecimal,
        testRunPublicId: PublicId,
        createdTimestamp: Instant,
    ) = withContext(Dispatchers.IO) {
        val normalizedProjectName = projectName ?: ""
        val createdTimestampLocal = LocalDateTime.ofInstant(createdTimestamp, ZoneOffset.UTC)

        dslContext.insertInto(REPOSITORY_CURRENT_COVERAGE)
            .set(REPOSITORY_CURRENT_COVERAGE.REPO_NAME, repoName)
            .set(REPOSITORY_CURRENT_COVERAGE.PROJECT_NAME, normalizedProjectName)
            .set(REPOSITORY_CURRENT_COVERAGE.BRANCH_NAME, branchName)
            .set(REPOSITORY_CURRENT_COVERAGE.COVERED_PERCENTAGE, coveredPercentage)
            .set(REPOSITORY_CURRENT_COVERAGE.TEST_RUN_PUBLIC_ID, testRunPublicId.id)
            .set(REPOSITORY_CURRENT_COVERAGE.CREATED_TIMESTAMP, createdTimestampLocal)
            .onConflict(REPOSITORY_CURRENT_COVERAGE.REPO_NAME, REPOSITORY_CURRENT_COVERAGE.PROJECT_NAME)
            .doUpdate()
            .set(REPOSITORY_CURRENT_COVERAGE.BRANCH_NAME, branchName)
            .set(REPOSITORY_CURRENT_COVERAGE.COVERED_PERCENTAGE, coveredPercentage)
            .set(REPOSITORY_CURRENT_COVERAGE.TEST_RUN_PUBLIC_ID, testRunPublicId.id)
            .set(REPOSITORY_CURRENT_COVERAGE.CREATED_TIMESTAMP, createdTimestampLocal)
            .set(REPOSITORY_CURRENT_COVERAGE.UPDATED_TIMESTAMP, LocalDateTime.now())
            .where(REPOSITORY_CURRENT_COVERAGE.CREATED_TIMESTAMP.lessThan(createdTimestampLocal))
            .execute()

        Unit
    }

    override suspend fun fetchLastKnownCoverage(
        repoName: String,
        projectName: String?,
    ): RepositoryCurrentCoverage? =
        withContext(Dispatchers.IO) {
            val normalizedProjectName = projectName ?: ""

            dslContext.selectFrom(REPOSITORY_CURRENT_COVERAGE)
                .where(
                    REPOSITORY_CURRENT_COVERAGE.REPO_NAME.eq(repoName)
                        .and(REPOSITORY_CURRENT_COVERAGE.PROJECT_NAME.eq(normalizedProjectName)),
                )
                .fetchOne()
                ?.let { record ->
                    RepositoryCurrentCoverage(
                        id = record.testRunPublicId,
                        createdTimestamp = record.createdTimestamp.toInstant(ZoneOffset.UTC),
                        coveredPercentage = record.coveredPercentage,
                        repo = record.repoName,
                        project = projectName,
                        branch = record.branchName,
                    )
                }
        }

    data class ReportTimelineEntry(
        val publicId: String,
        val createdTimestamp: Instant,
        val coverageStats: CoverageReportStats,
    ) {
        fun toFullEntry() =
            RepositoryCoverageTimelineEntry(
                publicId = publicId,
                createdTimestamp = createdTimestamp,
                coverageStats = coverageStats.toCoverageStats(null),
            )
    }
}
