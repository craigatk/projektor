package projektor.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.tables.daos.CodeCoverageGroupDao
import projektor.database.generated.tables.daos.CodeCoverageRunDao
import projektor.database.generated.tables.daos.CodeCoverageStatsDao
import projektor.database.generated.tables.daos.TestRunDao
import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.database.generated.tables.pojos.CodeCoverageStats
import projektor.parser.coverage.model.CoverageReport
import projektor.server.api.PublicId

class CoverageDatabaseRepository(private val dslContext: DSLContext) : CoverageRepository {
    private val codeCoverageRunDao = CodeCoverageRunDao(dslContext.configuration())
    private val testRunDao = TestRunDao(dslContext.configuration())

    override suspend fun createOrGetCoverageRun(publicId: PublicId): CodeCoverageRun =
            withContext(Dispatchers.IO) {
                val codeCoverageRuns = codeCoverageRunDao.fetchByTestRunPublicId(publicId.id)

                if (codeCoverageRuns.isNotEmpty()) {
                    codeCoverageRuns[0]
                } else {
                    val codeCoverageRun = CodeCoverageRun()
                    codeCoverageRun.testRunId = testRunDao.fetchOneByPublicId(publicId.id).id
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
}
