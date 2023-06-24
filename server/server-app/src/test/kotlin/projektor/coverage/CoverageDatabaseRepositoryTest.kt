package projektor.coverage

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.daos.CodeCoverageFileDao
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.math.BigDecimal
import kotlin.test.assertNotNull

class CoverageDatabaseRepositoryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `when one coverage report should fetch its overall stats`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport(
            "my-report",
            CoverageReportStats(
                statementStat = CoverageReportStat(75, 25),
                lineStat = CoverageReportStat(60, 0),
                branchStat = CoverageReportStat(33, 66)
            ),
            null
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

    @Test
    fun `should save coverage file stats`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport(
            "my-report",
            CoverageReportStats(
                statementStat = CoverageReportStat(75, 25),
                lineStat = CoverageReportStat(60, 0),
                branchStat = CoverageReportStat(33, 66)
            ),
            null
        )
        val coverageGroup = runBlocking { coverageDatabaseRepository.addCoverageReport(coverageRun, coverageReport) }

        val coverageReportFiles = listOf(
            CoverageFile(
                directoryName = "dir-1",
                fileName = "File1.kt",
                stats = CoverageStats(
                    statementStat = CoverageStat(75, 25),
                    lineStat = CoverageStat(60, 0),
                    branchStat = CoverageStat(33, 66)
                ),
                missedLines = arrayOf(12, 13),
                partialLines = arrayOf(14, 15),
                filePath = null
            ),
            CoverageFile(
                directoryName = "dir-2",
                fileName = "File2.kt",
                stats = CoverageStats(
                    statementStat = CoverageStat(80, 20),
                    lineStat = CoverageStat(55, 10),
                    branchStat = CoverageStat(12, 1)
                ),
                missedLines = arrayOf(18, 19, 20),
                partialLines = arrayOf(21),
                filePath = null
            )
        )

        runBlocking { coverageDatabaseRepository.insertCoverageFiles(coverageReportFiles, coverageRun, coverageGroup) }

        val codeCoverageFileDao = CodeCoverageFileDao(dslContext.configuration())
        val codeCoverageFiles = codeCoverageFileDao.fetchByCodeCoverageRunId(coverageRun.id)

        expectThat(codeCoverageFiles).hasSize(2)

        val file1 = codeCoverageFiles.find { it.fileName == "File1.kt" }
        expectThat(file1).isNotNull().and {
            get { directoryName }.isEqualTo("dir-1")
            get { missedLines.toList() }.hasSize(2).contains(12, 13)
            get { partialLines.toList() }.hasSize(2).contains(14, 15)
        }

        val file2 = codeCoverageFiles.find { it.fileName == "File2.kt" }
        expectThat(file2).isNotNull().and {
            get { directoryName }.isEqualTo("dir-2")
            get { missedLines.toList() }.hasSize(3).contains(18, 19, 20)
            get { partialLines.toList() }.hasSize(1).contains(21)
        }
    }

    @Test
    fun `should fetch code coverage files`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport(
            "my-report",
            CoverageReportStats(
                statementStat = CoverageReportStat(75, 25),
                lineStat = CoverageReportStat(60, 0),
                branchStat = CoverageReportStat(33, 66)
            ),
            null
        )
        val coverageGroup = runBlocking { coverageDatabaseRepository.addCoverageReport(coverageRun, coverageReport) }

        val coverageReportFiles = listOf(
            CoverageFile(
                directoryName = "dir-1",
                fileName = "File1.kt",
                stats = CoverageStats(
                    statementStat = CoverageStat(75, 25, null),
                    lineStat = CoverageStat(60, 0, null),
                    branchStat = CoverageStat(33, 66, null)
                ),
                missedLines = arrayOf(12),
                partialLines = arrayOf(14, 15),
                filePath = null
            ),
            CoverageFile(
                directoryName = "dir-2",
                fileName = "File2.kt",
                stats = CoverageStats(
                    statementStat = CoverageStat(80, 20, null),
                    lineStat = CoverageStat(55, 10, null),
                    branchStat = CoverageStat(12, 1, null)
                ),
                missedLines = arrayOf(18, 19, 20),
                partialLines = arrayOf(21),
                filePath = null
            )
        )

        runBlocking { coverageDatabaseRepository.insertCoverageFiles(coverageReportFiles, coverageRun, coverageGroup) }

        val coverageFiles = runBlocking { coverageDatabaseRepository.fetchCoverageFiles(publicId, "my-report") }
        expectThat(coverageFiles).hasSize(2)

        val coverageFile1 = coverageFiles.find { it.fileName == "File1.kt" }
        expectThat(coverageFile1).isNotNull().and {
            get { directoryName }.isEqualTo("dir-1")
            get { missedLines.toList() }.hasSize(1).contains(12)
            get { partialLines.toList() }.hasSize(2).contains(14, 15)
            get { stats }.and {
                get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("100.00"))
                get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("33.33"))
            }
        }

        val coverageFile2 = coverageFiles.find { it.fileName == "File2.kt" }
        expectThat(coverageFile2).isNotNull().and {
            get { directoryName }.isEqualTo("dir-2")
            get { missedLines.toList() }.hasSize(3).contains(18, 19, 20)
            get { partialLines.toList() }.hasSize(1).contains(21)
            get { stats }.and {
                get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("84.62"))
                get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("92.31"))
            }
        }
    }

    @Test
    fun `when coverage data should return true when checking if coverage exists`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport(
            "my-report",
            CoverageReportStats(
                statementStat = CoverageReportStat(75, 25),
                lineStat = CoverageReportStat(60, 0),
                branchStat = CoverageReportStat(33, 66)
            ),
            null
        )
        runBlocking { coverageDatabaseRepository.addCoverageReport(coverageRun, coverageReport) }

        val hasCoverageData = runBlocking { coverageDatabaseRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isTrue()
    }

    @Test
    fun `when no coverage run should return false when checking if coverage exists`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val hasCoverageData = runBlocking { coverageDatabaseRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isFalse()
    }

    @Test
    fun `when has coverage run but no coverage groups should return false when checking if coverage exists`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val hasCoverageData = runBlocking { coverageDatabaseRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isFalse()
    }

    @Test
    fun `when upserting new coverage group should create it`() {
        val coverageDatabaseRepository = CoverageDatabaseRepository(dslContext)

        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val coverageRun = runBlocking { coverageDatabaseRepository.createOrGetCoverageRun(publicId) }

        val coverageReport = CoverageReport(
            "my-report",
            CoverageReportStats(
                statementStat = CoverageReportStat(75, 25),
                lineStat = CoverageReportStat(60, 0),
                branchStat = CoverageReportStat(33, 66)
            ),
            null
        )

        val newLineStat = CoverageStat(covered = 8, missed = 1)

        val (newCoverageGroup, coverageGroupStatus) = runBlocking { coverageDatabaseRepository.upsertCoverageGroup(coverageRun, coverageReport, newLineStat) }
        expectThat(newCoverageGroup) {
            get { name }.isEqualTo("my-report")
        }
        expectThat(coverageGroupStatus).isEqualTo(CoverageGroupStatus.NEW)

        val newCoverageGroupsFromDB = runBlocking { coverageGroupDao.fetchByCodeCoverageRunId(coverageRun.id) }
        expectThat(newCoverageGroupsFromDB).hasSize(1).any {
            get { name }.isEqualTo("my-report")
        }
    }
}
