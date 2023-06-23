package projektor.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL.sum
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.CODE_COVERAGE_FILE
import projektor.database.generated.Tables.CODE_COVERAGE_GROUP
import projektor.database.generated.Tables.CODE_COVERAGE_RUN
import projektor.database.generated.Tables.CODE_COVERAGE_STATS
import projektor.database.generated.tables.daos.CodeCoverageFileDao
import projektor.database.generated.tables.daos.CodeCoverageGroupDao
import projektor.database.generated.tables.daos.CodeCoverageRunDao
import projektor.database.generated.tables.daos.CodeCoverageStatsDao
import projektor.database.generated.tables.pojos.CodeCoverageFile
import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.database.generated.tables.pojos.CodeCoverageStats
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import kotlin.streams.toList

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

    private val coverageFileMapper = JdbcMapperFactory.newInstance()
        .addKeys("id")
        .ignorePropertyNotFound()
        .newMapper(CoverageFile::class.java)

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

    override suspend fun addCoverageReport(
        coverageRun: CodeCoverageRun,
        coverageReport: CoverageReport
    ): CodeCoverageGroup =
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

    override suspend fun upsertCoverageGroup(
        coverageRun: CodeCoverageRun,
        coverageReport: CoverageReport,
        newLineStat: CoverageStat
    ): Pair<CodeCoverageGroup, CoverageGroupStatus> = withContext(Dispatchers.IO) {
        val existingCoverageGroup = dslContext
            .selectFrom(CODE_COVERAGE_GROUP)
            .where(
                CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID.eq(coverageRun.id)
                    .and(CODE_COVERAGE_GROUP.NAME.eq(coverageReport.name))
            )
            .fetchOneInto(CodeCoverageGroup::class.java)

        if (existingCoverageGroup == null) {
            Pair(addCoverageReport(coverageRun, coverageReport), CoverageGroupStatus.NEW)
        } else {
            val codeCoverageStatsDao = CodeCoverageStatsDao(dslContext.configuration())
            val existingGroupStats = codeCoverageStatsDao.fetchOneById(existingCoverageGroup.statsId)

            existingGroupStats.lineCovered = newLineStat.covered
            existingGroupStats.lineMissed = newLineStat.missed
            existingGroupStats.branchCovered = 0
            existingGroupStats.branchMissed = 0
            existingGroupStats.statementCovered = 0
            existingGroupStats.statementMissed = 0
            codeCoverageStatsDao.update(existingGroupStats)

            Pair(existingCoverageGroup, CoverageGroupStatus.EXISTING)
        }
    }

    override suspend fun insertCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup
    ) =
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val codeCoverageFileDao = CodeCoverageFileDao(configuration)
                val codeCoverageStatsDao = CodeCoverageStatsDao(configuration)

                coverageReportFiles.forEach { reportFile ->
                    val reportFileStats = reportFile.stats

                    val fileStats = CodeCoverageStats()
                    fileStats.codeCoverageRunId = coverageRun.id
                    fileStats.statementCovered = reportFileStats.statementStat.covered
                    fileStats.statementMissed = reportFileStats.statementStat.missed
                    fileStats.lineCovered = reportFileStats.lineStat.covered
                    fileStats.lineMissed = reportFileStats.lineStat.missed
                    fileStats.branchCovered = reportFileStats.branchStat.covered
                    fileStats.branchMissed = reportFileStats.branchStat.missed
                    fileStats.scope = "FILE"
                    codeCoverageStatsDao.insert(fileStats)

                    val codeCoverageFile = CodeCoverageFile()
                    codeCoverageFile.codeCoverageRunId = coverageRun.id
                    codeCoverageFile.codeCoverageGroupId = coverageGroup.id
                    codeCoverageFile.statsId = fileStats.id
                    codeCoverageFile.fileName = reportFile.fileName
                    codeCoverageFile.directoryName = reportFile.directoryName
                    codeCoverageFile.filePath = reportFile.filePath
                    codeCoverageFile.missedLines = reportFile.missedLines
                    codeCoverageFile.partialLines = reportFile.partialLines

                    codeCoverageFileDao.insert(codeCoverageFile)
                }
            }
        }

    override suspend fun upsertCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup
    ) = withContext(Dispatchers.IO) {
        val currentCoverageFilePaths: List<String> = dslContext
            .select(CODE_COVERAGE_FILE.FILE_PATH)
            .from(CODE_COVERAGE_FILE)
            .where(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(coverageGroup.id))
            .fetchInto(String::class.java)

        val existingCoverageFiles = coverageReportFiles.filter { currentCoverageFilePaths.contains(it.filePath) }
        val newCoverageFiles = coverageReportFiles.filter { !currentCoverageFilePaths.contains(it.filePath) }

        insertCoverageFiles(newCoverageFiles, coverageRun, coverageGroup)

        updateCoverageFiles(existingCoverageFiles, coverageRun, coverageGroup)
    }

    private suspend fun updateCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup
    ) = withContext(Dispatchers.IO) {
        val codeCoverageFileDao = CodeCoverageFileDao(dslContext.configuration())

        coverageReportFiles.forEach { reportFile ->
            val codeCoverageFile = dslContext
                .selectFrom(CODE_COVERAGE_FILE)
                .where(
                    CODE_COVERAGE_FILE.FILE_PATH.eq(reportFile.filePath)
                        .and(CODE_COVERAGE_FILE.CODE_COVERAGE_RUN_ID.eq(coverageRun.id))
                        .and(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(coverageGroup.id))
                )
                .fetchOneInto(CodeCoverageFile::class.java)

            if (codeCoverageFile != null) {
                codeCoverageFile.missedLines = reportFile.missedLines
                codeCoverageFile.partialLines = arrayOf()
                codeCoverageFileDao.update(codeCoverageFile)

                dslContext.update(CODE_COVERAGE_STATS)
                    .set(CODE_COVERAGE_STATS.LINE_COVERED, reportFile.stats.lineStat.covered)
                    .set(CODE_COVERAGE_STATS.LINE_MISSED, reportFile.stats.lineStat.missed)
                    .set(CODE_COVERAGE_STATS.BRANCH_COVERED, 0)
                    .set(CODE_COVERAGE_STATS.BRANCH_MISSED, 0)
                    .set(CODE_COVERAGE_STATS.STATEMENT_COVERED, 0)
                    .set(CODE_COVERAGE_STATS.STATEMENT_MISSED, 0)
                    .where(CODE_COVERAGE_STATS.ID.eq(codeCoverageFile.statsId))
                    .execute()
            }
        }
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

    override suspend fun coverageGroupExists(publicId: PublicId): Boolean =
        withContext(Dispatchers.IO) {
            dslContext.fetchExists(
                dslContext.select(CODE_COVERAGE_RUN.ID)
                    .from(CODE_COVERAGE_RUN)
                    .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_RUN.ID.eq(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID))
                    .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id))
            )
        }

    override suspend fun fetchCoverageFiles(publicId: PublicId, groupName: String): List<CoverageFile> =
        withContext(Dispatchers.IO) {
            val resultSet = dslContext.select(
                CODE_COVERAGE_FILE.DIRECTORY_NAME,
                CODE_COVERAGE_FILE.FILE_NAME,
                CODE_COVERAGE_FILE.FILE_PATH,
                CODE_COVERAGE_FILE.MISSED_LINES,
                CODE_COVERAGE_FILE.PARTIAL_LINES,
                CODE_COVERAGE_STATS.BRANCH_COVERED.`as`("stats_branch_stat_covered"),
                CODE_COVERAGE_STATS.BRANCH_MISSED.`as`("stats_branch_stat_missed"),
                CODE_COVERAGE_STATS.STATEMENT_COVERED.`as`("stats_statement_stat_covered"),
                CODE_COVERAGE_STATS.STATEMENT_MISSED.`as`("stats_statement_stat_missed"),
                CODE_COVERAGE_STATS.LINE_COVERED.`as`("stats_line_stat_covered"),
                CODE_COVERAGE_STATS.LINE_MISSED.`as`("stats_line_stat_missed")
            )
                .from(CODE_COVERAGE_FILE)
                .innerJoin(CODE_COVERAGE_RUN).on(CODE_COVERAGE_FILE.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(CODE_COVERAGE_GROUP.ID))
                .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_FILE.STATS_ID.eq(CODE_COVERAGE_STATS.ID))
                .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_GROUP.NAME.eq(groupName)))
                .fetchResultSet()

            val coverageFiles: List<CoverageFile> = resultSet.use {
                coverageFileMapper.stream(resultSet).toList()
            }

            coverageFiles
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

    override suspend fun deleteCoverage(publicId: PublicId): Boolean =
        withContext(Dispatchers.IO) {
            dslContext.deleteFrom(CODE_COVERAGE_RUN)
                .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id))
                .execute() > 0
        }
}
