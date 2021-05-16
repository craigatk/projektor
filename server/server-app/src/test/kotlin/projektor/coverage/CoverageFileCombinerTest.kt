package projektor.coverage

import org.junit.jupiter.api.Test
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class CoverageFileCombinerTest {
    @Test
    fun `should add new files that do not currently exist`() {
        val existingCoverageFiles = listOf(
            createCoverageFile("src/File1.tsx", arrayOf(2), CoverageStat(covered = 5, missed = 1)),
            createCoverageFile("src/File2.tsx", arrayOf(), CoverageStat(covered = 6, missed = 0)),
            createCoverageFile("src/File3.tsx", arrayOf(), CoverageStat(covered = 7, missed = 0))
        )

        val incomingCoverageFiles = listOf(
            createCoverageFile("src/File8.tsx", arrayOf(2, 6), CoverageStat(covered = 5, missed = 2)),
            createCoverageFile("src/File9.tsx", arrayOf(), CoverageStat(covered = 9, missed = 0))
        )

        val combinedCoverageFiles = combineCoverageFiles(existingCoverageFiles, incomingCoverageFiles)
        expectThat(combinedCoverageFiles).hasSize(5)

        expectThat(combinedCoverageFiles.find { it.filePath == "src/File1.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf(2))
            get { stats.lineStat.covered }.isEqualTo(5)
            get { stats.lineStat.missed }.isEqualTo(1)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File2.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf())
            get { stats.lineStat.covered }.isEqualTo(6)
            get { stats.lineStat.missed }.isEqualTo(0)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File3.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf())
            get { stats.lineStat.covered }.isEqualTo(7)
            get { stats.lineStat.missed }.isEqualTo(0)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File8.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf(2, 6))
            get { stats.lineStat.covered }.isEqualTo(5)
            get { stats.lineStat.missed }.isEqualTo(2)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File9.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf())
            get { stats.lineStat.covered }.isEqualTo(9)
            get { stats.lineStat.missed }.isEqualTo(0)
        }
    }

    @Test
    fun `should combine existing file and add new one`() {
        val existingCoverageFiles = listOf(
            createCoverageFile("src/File1.tsx", arrayOf(2), CoverageStat(covered = 5, missed = 1)),
            createCoverageFile("src/File2.tsx", arrayOf(3), CoverageStat(covered = 6, missed = 1)),
        )

        val incomingCoverageFiles = listOf(
            createCoverageFile("src/File1.tsx", arrayOf(6), CoverageStat(covered = 5, missed = 1)),
            createCoverageFile("src/File3.tsx", arrayOf(), CoverageStat(covered = 9, missed = 0))
        )

        val combinedCoverageFiles = combineCoverageFiles(existingCoverageFiles, incomingCoverageFiles)
        expectThat(combinedCoverageFiles).hasSize(3)

        expectThat(combinedCoverageFiles.find { it.filePath == "src/File1.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf())
            get { stats.lineStat.covered }.isEqualTo(6)
            get { stats.lineStat.missed }.isEqualTo(0)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File2.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf(3))
            get { stats.lineStat.covered }.isEqualTo(6)
            get { stats.lineStat.missed }.isEqualTo(1)
        }
        expectThat(combinedCoverageFiles.find { it.filePath == "src/File3.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf())
            get { stats.lineStat.covered }.isEqualTo(9)
            get { stats.lineStat.missed }.isEqualTo(0)
        }
    }

    @Test
    fun `should use the intersection of missed lines as the result`() {
        val existingCoverageFiles = listOf(
            createCoverageFile("src/File1.tsx", arrayOf(2, 6, 8, 9), CoverageStat(covered = 5, missed = 4)),
        )

        val incomingCoverageFiles = listOf(
            createCoverageFile("src/File1.tsx", arrayOf(1, 6, 8, 10), CoverageStat(covered = 5, missed = 4)),
        )

        val combinedCoverageFiles = combineCoverageFiles(existingCoverageFiles, incomingCoverageFiles)
        expectThat(combinedCoverageFiles).hasSize(1)

        expectThat(combinedCoverageFiles.find { it.filePath == "src/File1.tsx" }).isNotNull().and {
            get { missedLines }.isEqualTo(arrayOf(6, 8))
            get { stats.lineStat.covered }.isEqualTo(7)
            get { stats.lineStat.missed }.isEqualTo(2)
        }
    }

    private fun createCoverageFile(filePath: String, missedLines: Array<Int>, lineStat: CoverageStat) =
        CoverageFile(
            filePath = filePath,
            fileName = "fileName",
            directoryName = "dir",
            missedLines = missedLines,
            partialLines = arrayOf(),
            stats = CoverageStats(
                lineStat = lineStat,
                branchStat = CoverageStat(0, 0),
                statementStat = CoverageStat(0, 0)
            )
        )
}
