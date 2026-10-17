package projektor.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.Condition
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL
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
import projektor.util.lockUntilTransactionEnds
import kotlin.streams.toList

class CoverageDatabaseRepository(private val dslContext: DSLContext) : CoverageRepository {
    private val coverageReportMapper =
        JdbcMapperFactory.newInstance()
            .addKeys("id")
            .ignorePropertyNotFound()
            .newMapper(CoverageReport::class.java)

    private val overallStatsMapper =
        JdbcMapperFactory.newInstance()
            .addKeys("id")
            .ignorePropertyNotFound()
            .newMapper(CoverageReportStats::class.java)

    private val coverageFileMapper =
        JdbcMapperFactory.newInstance()
            .addKeys("id")
            .ignorePropertyNotFound()
            .newMapper(CoverageFile::class.java)

    override suspend fun createOrGetCoverageRun(publicId: PublicId) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val transactionContext = DSL.using(configuration)
                transactionContext.lockUntilTransactionEnds(coverageLockKey(publicId))

                fetchOrCreateCoverageRun(transactionContext, publicId)
            }
        }
    }

    override suspend fun addCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        coverageReportFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val transactionContext = DSL.using(configuration)
                transactionContext.lockUntilTransactionEnds(coverageLockKey(publicId))

                val coverageRun = fetchOrCreateCoverageRun(transactionContext, publicId)
                val coverageGroup = insertCoverageGroup(coverageRun, coverageReport, configuration)

                insertCoverageFiles(coverageReportFiles, coverageRun, coverageGroup, configuration)
            }
        }
    }

    override suspend fun appendCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        incomingCoverageFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            dslContext.transaction { configuration ->
                val transactionContext = DSL.using(configuration)
                transactionContext.lockUntilTransactionEnds(coverageLockKey(publicId))

                val coverageRun = fetchOrCreateCoverageRun(transactionContext, publicId)
                val existingCoverageGroup = fetchCoverageGroup(transactionContext, coverageRun, coverageReport.name)

                if (existingCoverageGroup == null) {
                    val coverageGroup = insertCoverageGroup(coverageRun, coverageReport, configuration)
                    insertCoverageFiles(incomingCoverageFiles, coverageRun, coverageGroup, configuration)
                } else {
                    val existingCoverageFiles =
                        fetchCoverageFiles(transactionContext, CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(existingCoverageGroup.id))
                    val combinedCoverageFiles = combineCoverageFiles(existingCoverageFiles, incomingCoverageFiles)

                    val newLineStat =
                        CoverageStat(
                            covered = combinedCoverageFiles.sumOf { it.stats.lineStat.covered },
                            missed = combinedCoverageFiles.sumOf { it.stats.lineStat.missed },
                        )
                    updateGroupLineStat(transactionContext, existingCoverageGroup, newLineStat)

                    upsertCoverageFiles(transactionContext, combinedCoverageFiles, coverageRun, existingCoverageGroup)
                }
            }
        }
    }

    private fun fetchOrCreateCoverageRun(
        transactionContext: DSLContext,
        publicId: PublicId,
    ): CodeCoverageRun {
        val codeCoverageRunDao = CodeCoverageRunDao(transactionContext.configuration())
        val codeCoverageRuns = codeCoverageRunDao.fetchByTestRunPublicId(publicId.id)

        return if (codeCoverageRuns.isNotEmpty()) {
            codeCoverageRuns[0]
        } else {
            val codeCoverageRun = CodeCoverageRun()
            codeCoverageRun.testRunPublicId = publicId.id
            codeCoverageRunDao.insert(codeCoverageRun)

            codeCoverageRun
        }
    }

    private fun fetchCoverageGroup(
        transactionContext: DSLContext,
        coverageRun: CodeCoverageRun,
        groupName: String,
    ): CodeCoverageGroup? =
        transactionContext
            .selectFrom(CODE_COVERAGE_GROUP)
            .where(
                CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID.eq(coverageRun.id)
                    .and(CODE_COVERAGE_GROUP.NAME.eq(groupName)),
            )
            .fetchOneInto(CodeCoverageGroup::class.java)

    // Appended coverage only tracks line stats, so the statement and branch stats are zeroed
    private fun updateGroupLineStat(
        transactionContext: DSLContext,
        coverageGroup: CodeCoverageGroup,
        newLineStat: CoverageStat,
    ) {
        val codeCoverageStatsDao = CodeCoverageStatsDao(transactionContext.configuration())
        val existingGroupStats = codeCoverageStatsDao.fetchOneById(coverageGroup.statsId)

        existingGroupStats.lineCovered = newLineStat.covered
        existingGroupStats.lineMissed = newLineStat.missed
        existingGroupStats.branchCovered = 0
        existingGroupStats.branchMissed = 0
        existingGroupStats.statementCovered = 0
        existingGroupStats.statementMissed = 0
        codeCoverageStatsDao.update(existingGroupStats)
    }

    private fun insertCoverageGroup(
        coverageRun: CodeCoverageRun,
        coverageReport: CoverageReport,
        configuration: Configuration,
    ): CodeCoverageGroup {
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

        val codeCoverageGroup = CodeCoverageGroup()
        codeCoverageGroup.codeCoverageRunId = coverageRun.id
        codeCoverageGroup.name = coverageReport.name
        codeCoverageGroup.statsId = groupStats.id
        codeCoverageGroupDao.insert(codeCoverageGroup)

        return codeCoverageGroup
    }

    private fun insertCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup,
        configuration: Configuration,
    ) {
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

    /**
     * Inserts files whose path isn't in the group yet. For files already in the group, updates their missed lines
     * and line stats (and clears their partial lines and zeroes their statement and branch stats).
     */
    private fun upsertCoverageFiles(
        transactionContext: DSLContext,
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup,
    ) {
        val currentCoverageFilePaths: List<String> =
            transactionContext
                .select(CODE_COVERAGE_FILE.FILE_PATH)
                .from(CODE_COVERAGE_FILE)
                .where(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(coverageGroup.id))
                .fetchInto(String::class.java)

        val existingCoverageFiles = coverageReportFiles.filter { currentCoverageFilePaths.contains(it.filePath) }
        val newCoverageFiles = coverageReportFiles.filter { !currentCoverageFilePaths.contains(it.filePath) }

        insertCoverageFiles(newCoverageFiles, coverageRun, coverageGroup, transactionContext.configuration())

        updateCoverageFiles(transactionContext, existingCoverageFiles, coverageRun, coverageGroup)
    }

    private fun updateCoverageFiles(
        transactionContext: DSLContext,
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup,
    ) {
        val codeCoverageFileDao = CodeCoverageFileDao(transactionContext.configuration())

        coverageReportFiles.forEach { reportFile ->
            val codeCoverageFile =
                transactionContext
                    .selectFrom(CODE_COVERAGE_FILE)
                    .where(
                        CODE_COVERAGE_FILE.FILE_PATH.eq(reportFile.filePath)
                            .and(CODE_COVERAGE_FILE.CODE_COVERAGE_RUN_ID.eq(coverageRun.id))
                            .and(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(coverageGroup.id)),
                    )
                    .fetchOneInto(CodeCoverageFile::class.java)

            if (codeCoverageFile != null) {
                codeCoverageFile.missedLines = reportFile.missedLines
                codeCoverageFile.partialLines = arrayOf()
                codeCoverageFileDao.update(codeCoverageFile)

                transactionContext.update(CODE_COVERAGE_STATS)
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
            val resultSet =
                dslContext.select(
                    sum(CODE_COVERAGE_STATS.BRANCH_COVERED).`as`("branch_stat_covered"),
                    sum(CODE_COVERAGE_STATS.BRANCH_MISSED).`as`("branch_stat_missed"),
                    sum(CODE_COVERAGE_STATS.STATEMENT_COVERED).`as`("statement_stat_covered"),
                    sum(CODE_COVERAGE_STATS.STATEMENT_MISSED).`as`("statement_stat_missed"),
                    sum(CODE_COVERAGE_STATS.LINE_COVERED).`as`("line_stat_covered"),
                    sum(CODE_COVERAGE_STATS.LINE_MISSED).`as`("line_stat_missed"),
                )
                    .from(CODE_COVERAGE_STATS)
                    .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_STATS.ID.eq(CODE_COVERAGE_GROUP.STATS_ID))
                    .innerJoin(CODE_COVERAGE_RUN).on(CODE_COVERAGE_RUN.ID.eq(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID))
                    .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")))
                    .fetchResultSet()

            val overallStatus: CoverageReportStats =
                resultSet.use {
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
                    .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id)),
            )
        }

    override suspend fun fetchCoverageFiles(
        publicId: PublicId,
        groupName: String,
    ): List<CoverageFile> =
        withContext(Dispatchers.IO) {
            fetchCoverageFiles(dslContext, CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_GROUP.NAME.eq(groupName)))
        }

    private fun fetchCoverageFiles(
        dslContext: DSLContext,
        condition: Condition,
    ): List<CoverageFile> {
        val resultSet =
            dslContext.select(
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
                CODE_COVERAGE_STATS.LINE_MISSED.`as`("stats_line_stat_missed"),
            )
                .from(CODE_COVERAGE_FILE)
                .innerJoin(CODE_COVERAGE_RUN).on(CODE_COVERAGE_FILE.CODE_COVERAGE_RUN_ID.eq(CODE_COVERAGE_RUN.ID))
                .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_FILE.CODE_COVERAGE_GROUP_ID.eq(CODE_COVERAGE_GROUP.ID))
                .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_FILE.STATS_ID.eq(CODE_COVERAGE_STATS.ID))
                .where(condition)
                .fetchResultSet()

        return resultSet.use { coverageFileMapper.stream(resultSet).toList() }
    }

    override suspend fun fetchCoverageList(publicId: PublicId): List<CoverageReport> =
        withContext(Dispatchers.IO) {
            val resultSet =
                dslContext.select(
                    CODE_COVERAGE_GROUP.NAME.`as`("name"),
                    CODE_COVERAGE_STATS.BRANCH_COVERED.`as`("total_stats_branch_stat_covered"),
                    CODE_COVERAGE_STATS.BRANCH_MISSED.`as`("total_stats_branch_stat_missed"),
                    CODE_COVERAGE_STATS.STATEMENT_COVERED.`as`("total_stats_statement_stat_covered"),
                    CODE_COVERAGE_STATS.STATEMENT_MISSED.`as`("total_stats_statement_stat_missed"),
                    CODE_COVERAGE_STATS.LINE_COVERED.`as`("total_stats_line_stat_covered"),
                    CODE_COVERAGE_STATS.LINE_MISSED.`as`("total_stats_line_stat_missed"),
                )
                    .from(CODE_COVERAGE_RUN)
                    .innerJoin(CODE_COVERAGE_GROUP).on(CODE_COVERAGE_RUN.ID.eq(CODE_COVERAGE_GROUP.CODE_COVERAGE_RUN_ID))
                    .innerJoin(CODE_COVERAGE_STATS).on(CODE_COVERAGE_GROUP.STATS_ID.eq(CODE_COVERAGE_STATS.ID))
                    .where(CODE_COVERAGE_RUN.TEST_RUN_PUBLIC_ID.eq(publicId.id).and(CODE_COVERAGE_STATS.SCOPE.eq("GROUP")))
                    .fetchResultSet()

            val coverageReports: List<CoverageReport> =
                resultSet.use {
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

    companion object {
        private fun coverageLockKey(publicId: PublicId) = "coverage:${publicId.id}"
    }
}
