package projektor.coverage

import java.math.BigDecimal
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CoverageDatabaseRepositoryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `when one coverage report should fetch its overall stats`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport("my-report",
                CoverageReportStats(
                        statementStat = CoverageReportStat(75, 25),
                        lineStat = CoverageReportStat(60, 0),
                        branchStat = CoverageReportStat(33, 66)
                )
        )
        runBlocking { coverageDatabaseRepository.addCoverageReport(coverageRun, coverageReport) }

        val overallStats = runBlocking { coverageDatabaseRepository.fetchOverallStats(publicId) }
        assertNotNull(overallStats)

        expectThat(overallStats) {
            get { statementStat.covered }.isEqualTo(75)
            get { statementStat.missed }.isEqualTo(25)
            get { statementStat.total }.isEqualTo(100)
            get { statementStat.percentCovered }.isEqualTo(BigDecimal("75.00"))

            get { lineStat.covered }.isEqualTo(60)
            get { lineStat.missed }.isEqualTo(0)
            get { lineStat.total }.isEqualTo(60)
            get { lineStat.percentCovered }.isEqualTo(BigDecimal("100.00"))

            get { branchStat.covered }.isEqualTo(33)
            get { branchStat.missed }.isEqualTo(66)
            get { branchStat.total }.isEqualTo(99)
            get { branchStat.percentCovered }.isEqualTo(BigDecimal("33.33"))
        }
    }
}
