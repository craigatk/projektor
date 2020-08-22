package projektor.coverage

import kotlin.streams.toList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL.sum
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.*
import projektor.database.generated.tables.daos.CodeCoverageGroupDao
import projektor.database.generated.tables.daos.CodeCoverageRunDao
import projektor.database.generated.tables.daos.CodeCoverageStatsDao
import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.database.generated.tables.pojos.CodeCoverageStats
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId

class CoverageDatabaseRepository(private val dslContext: DSLContext) : CoverageRepository {
    private val codeCoverageRunDao = CodeCoverageRunDao(dslContext.configuration())

    private val coverageReportMapper = JdbcMapperFactory.newInstance()
            .addKeys("id")
            .ignorePropertyNotFound()
            .newMapper(CoverageReport::class.java)

    private val overallStatsMapper = JdbcMapperFactory.newInstance()
            .addKeys("id")
            .ignorePropertyNotFound()
            .newMapper(CoverageReportStats::class.java)

    override suspend fun createOrGetCoverageRun(publicId: PublicId): CodeCoverageRun =
            withContext(Dispatchers.IO) {
                val codeCoverageRuns = codeCoverageRunDao.fetchByTestRunPublicId(publicId.id)

                if (codeCoverageRuns.isNotEmpty()) {
                    codeCoverageRuns[0]
                } else {
                    val codeCoverageRun = CodeCoverageRun()
                    codeCoverageRun.testRunPublicId = publicId.id
                    codeCoverageRunDao.insert(codeCoverageRun)

                    codeCoverageRun
                }
            }

    override suspend fun addCoverageReport(coverageRun: CodeCoverageRun, coverageReport: CoverageReport): CodeCoverageGroup =
            withContext(Dispatchers.IO) {
                val codeCoverageGroup = CodeCoverageGroup()

                dslContext.transaction { configuration ->
                    val codeCoverageGroupDao = CodeCoverageGroupDao(configuration)
                    val codeCoverageStatsDao = CodeCoverageStatsDao(configuration)

                    val groupStats = CodeCoverageStats()
                    groupStats.codeCoverageRunId = coverageRun.id
                    groupStats.statementCovered = coverageReport.totalStats.statementStat.covered
                    groupStats.statementMissed = coverageReport.totalStats.statementStat.missed
                    groupStats.lineCovered = coverageReport.totalStats.lineStat.covered
                    groupStats.lineMissed = coverageReport.totalStats.lineStat.missed
                    groupStats.branchCovered = coverageReport.totalStats.branchStat.covered
                    groupStats.branchMissed = coverageReport.totalStats.branchStat.missed
                    groupStats.scope = "GROUP"
                    codeCoverageStatsDao.insert(groupStats)

                    codeCoverageGroup.codeCoverageRunId = coverageRun.id
                    codeCoverageGroup.name = coverageReport.name
                    codeCoverageGroup.statsId = groupStats.id
                    codeCoverageGroupDao.insert(codeCoverageGroup)
                }

                codeCoverageGroup
            }

    override suspend fun fetchOverallStats(publicId: PublicId): CoverageReportStats =
            withContext(Dispatchers.IO) {
                val resultSet = dslContext.select(
                                sum(CODE_COVERAGE_STATS.BRANCH_COVERED).`as`("branch_stat_covered"),
                                sum(CODE_COVERAGE_STATS.BRANCH_MISSED).`as`("branch_stat_missed"),
                                sum(CODE_COVERAGE_STATS.STATEMENT_COVERED).`as`("statement_stat_covered"),
                                sum(CODE_COVERAGE_STATS.STATEMENT_MISSED).`as`("statement_stat_missed"),
                                sum(CODE_COVERAGE_STATS.LINE_COVERED).`as`("line_stat_covered"),
                                sum(CODE_COVERAGE_STATS.LINE_MISSED).`as`("line_stat_missed")
                        )
                        .from(CODE_COVERAGE_STATS)
                        .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_STATS.ID.eq(CODE_COVERAGE_GROUP.STATS_ID))
                        .innerJoin(CODE_COVERAGE_RUN).on(CODE_COVERAGE_RUN.ID.eq(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID))
                        .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")))
                        .fetchResultSet()

                val overallStatus: CoverageReportStats = resultSet.use {
                    overallStatsMapper.stream(resultSet).findFirst().orElse(null)
                }

                overallStatus
            }

    override suspend fun coverageExists(publicId: PublicId): Boolean =
            withContext(Dispatchers.IO) {
                dslContext.fetchExists(
                        dslContext.selectFrom(CODE_COVERAGE_RUN)
                                .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id)))
            }

    override suspend fun fetchCoverageList(publicId: PublicId): List<CoverageReport> =
            withContext(Dispatchers.IO) {
                val resultSet = dslContext.select(
                                CODE_COVERAGE_GROUP.NAME.`as`("name"),
                                CODE_COVERAGE_STATS.BRANCH_COVERED.`as`("total_stats_branch_stat_covered"),
                                CODE_COVERAGE_STATS.BRANCH_MISSED.`as`("total_stats_branch_stat_missed"),
                                CODE_COVERAGE_STATS.STATEMENT_COVERED.`as`("total_stats_statement_stat_covered"),
                                CODE_COVERAGE_STATS.STATEMENT_MISSED.`as`("total_stats_statement_stat_missed"),
                                CODE_COVERAGE_STATS.LINE_COVERED.`as`("total_stats_line_stat_covered"),
                                CODE_COVERAGE_STATS.LINE_MISSED.`as`("total_stats_line_stat_missed")
                        )
                        .from(CODE_COVERAGE_RUN)
                        .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_RUN.ID.eq(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID))
                        .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_GROUP.STATS_ID.eq(CODE_COVERAGE_STATS.ID))
                        .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")))
                        .fetchResultSet()

                val coverageReports: List<CoverageReport> = resultSet.use {
                    coverageReportMapper.stream(resultSet).toList()
                }

                coverageReports
            }
}
