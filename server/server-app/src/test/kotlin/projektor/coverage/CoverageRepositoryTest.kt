package projektor.coverage

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
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

abstract class CoverageRepositoryTest : RepositoryTestCase() {
    @Test
    fun `when one coverage report should fetch its overall stats`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )
        runBlocking { coverageRepository.addCoverageReport(publicId, coverageReport, listOf()) }

        val overallStats = runBlocking { coverageRepository.fetchOverallStats(publicId) }
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
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )
        runBlocking { coverageRepository.addCoverageReport(publicId, coverageReport, listOf()) }

        val coverageReportFiles =
            listOf(
                CoverageFile(
                    directoryName = "dir-1",
                    fileName = "File1.kt",
                    stats =
                        CoverageStats(
                            statementStat = CoverageStat(75, 25),
                            lineStat = CoverageStat(60, 0),
                            branchStat = CoverageStat(33, 66),
                        ),
                    missedLines = arrayOf(12, 13),
                    partialLines = arrayOf(14, 15),
                    filePath = null,
                ),
                CoverageFile(
                    directoryName = "dir-2",
                    fileName = "File2.kt",
                    stats =
                        CoverageStats(
                            statementStat = CoverageStat(80, 20),
                            lineStat = CoverageStat(55, 10),
                            branchStat = CoverageStat(12, 1),
                        ),
                    missedLines = arrayOf(18, 19, 20),
                    partialLines = arrayOf(21),
                    filePath = null,
                ),
            )

        runBlocking { coverageRepository.appendCoverageReport(publicId, coverageReport, coverageReportFiles) }

        val codeCoverageFiles = runBlocking { coverageRepository.fetchCoverageFiles(publicId, "my-report") }

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
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )
        runBlocking { coverageRepository.addCoverageReport(publicId, coverageReport, listOf()) }

        val coverageReportFiles =
            listOf(
                CoverageFile(
                    directoryName = "dir-1",
                    fileName = "File1.kt",
                    stats =
                        CoverageStats(
                            statementStat = CoverageStat(75, 25, null),
                            lineStat = CoverageStat(60, 0, null),
                            branchStat = CoverageStat(33, 66, null),
                        ),
                    missedLines = arrayOf(12),
                    partialLines = arrayOf(14, 15),
                    filePath = null,
                ),
                CoverageFile(
                    directoryName = "dir-2",
                    fileName = "File2.kt",
                    stats =
                        CoverageStats(
                            statementStat = CoverageStat(80, 20, null),
                            lineStat = CoverageStat(55, 10, null),
                            branchStat = CoverageStat(12, 1, null),
                        ),
                    missedLines = arrayOf(18, 19, 20),
                    partialLines = arrayOf(21),
                    filePath = null,
                ),
            )

        runBlocking { coverageRepository.appendCoverageReport(publicId, coverageReport, coverageReportFiles) }

        val coverageFiles = runBlocking { coverageRepository.fetchCoverageFiles(publicId, "my-report") }
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
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )
        runBlocking { coverageRepository.addCoverageReport(publicId, coverageReport, listOf()) }

        val hasCoverageData = runBlocking { coverageRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isTrue()
    }

    @Test
    fun `when no coverage run should return false when checking if coverage exists`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val hasCoverageData = runBlocking { coverageRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isFalse()
    }

    @Test
    fun `when has coverage run but no coverage groups should return false when checking if coverage exists`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        runBlocking { coverageRepository.createOrGetCoverageRun(publicId) }

        val hasCoverageData = runBlocking { coverageRepository.coverageGroupExists(publicId) }

        expectThat(hasCoverageData).isFalse()
    }

    @Test
    fun `when upserting new coverage group should create it`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )

        val reportFiles = listOf(coverageFile("File1.kt", "path/File1.kt", listOf(3), 9))

        runBlocking { coverageRepository.appendCoverageReport(publicId, coverageReport, reportFiles) }

        // A new group keeps the report's stats
        val newCoverageGroups = runBlocking { coverageRepository.fetchCoverageList(publicId) }
        expectThat(newCoverageGroups).hasSize(1).any {
            get { name }.isEqualTo("my-report")
            get { totalStats.statementStat }.isEqualTo(CoverageReportStat(75, 25))
            get { totalStats.lineStat }.isEqualTo(CoverageReportStat(60, 0))
        }
        expectThat(runBlocking { coverageRepository.fetchCoverageFiles(publicId, "my-report") }).hasSize(1)
    }

    @Test
    fun `when appending to existing coverage group should combine files and update line stats`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val coverageReport =
            CoverageReport(
                "my-report",
                CoverageReportStats(
                    statementStat = CoverageReportStat(75, 25),
                    lineStat = CoverageReportStat(60, 0),
                    branchStat = CoverageReportStat(33, 66),
                ),
                null,
            )

        runBlocking {
            coverageRepository.appendCoverageReport(
                publicId,
                coverageReport,
                listOf(coverageFile("File1.kt", "path/File1.kt", listOf(3, 4), 10)),
            )
            coverageRepository.appendCoverageReport(
                publicId,
                coverageReport,
                listOf(
                    coverageFile("File1.kt", "path/File1.kt", listOf(4, 5), 10),
                    coverageFile("File2.kt", "path/File2.kt", listOf(1), 5),
                ),
            )
        }

        val files = runBlocking { coverageRepository.fetchCoverageFiles(publicId, "my-report") }.associateBy { it.fileName }
        // Only lines missed by both reports are still missed
        expectThat(files["File1.kt"]?.missedLines?.toList()).isEqualTo(listOf(4))
        expectThat(files["File1.kt"]?.stats?.lineStat).isEqualTo(CoverageStat(9, 1))
        expectThat(files["File2.kt"]?.stats?.lineStat).isEqualTo(CoverageStat(4, 1))

        // The group's line stats are the sum of its files, and its statement and branch stats are zeroed
        val groups = runBlocking { coverageRepository.fetchCoverageList(publicId) }
        expectThat(groups).hasSize(1)
        expectThat(groups[0].totalStats.lineStat).isEqualTo(CoverageReportStat(13, 2))
        expectThat(groups[0].totalStats.statementStat).isEqualTo(CoverageReportStat(0, 0))
    }

    private fun coverageFile(
        fileName: String,
        filePath: String,
        missedLines: List<Int>,
        totalLines: Int,
    ) = CoverageFile(
        directoryName = "dir",
        fileName = fileName,
        stats =
            CoverageStats(
                statementStat = CoverageStat(totalLines - missedLines.size, missedLines.size),
                lineStat = CoverageStat(totalLines - missedLines.size, missedLines.size),
                branchStat = CoverageStat(0, 0),
            ),
        missedLines = missedLines.toTypedArray(),
        partialLines = arrayOf(),
        filePath = filePath,
    )
}

class CoverageRepositoryDatabaseTest : CoverageRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class CoverageRepositoryObjectStoreTest : CoverageRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
