package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportFile
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats

object CoverageReportFilesCombiner {
    fun combineFilesIntoReport(name: String, allFiles: List<CoverageReportFile>): CoverageReport {
        val uniqueFiles = if (allFiles.any { it.filePath == null }) {
            allFiles
        } else {
            val uniqueFilePaths = allFiles.mapNotNull { it.filePath }

            uniqueFilePaths.map { filePath ->
                val filesWithPath = allFiles.filter { it.filePath == filePath }

                if (filesWithPath.size == 1) {
                    filesWithPath[0]
                } else {
                    val linesTotalCount = filesWithPath[0].stats.lineStat.total
                    val branchesTotalCount = filesWithPath[0].stats.branchStat.total

                    val partialLines = filesWithPath.flatMap { it.partialLines }.distinct()
                    val missedLines = filesWithPath.flatMap { it.missedLines }.distinct() - partialLines

                    val lineStat = CoverageReportStat(covered = linesTotalCount - missedLines.size, missed = missedLines.size)
                    val branchStat = CoverageReportStat(covered = branchesTotalCount - partialLines.size)
                }
            }
        }

        val totalStats = CoverageReportStats(
                lineStat = CoverageReportStat(covered = 1, missed = 1),
                statementStat = CoverageReportStat(covered = 1, missed = 1),
                branchStat = CoverageReportStat(covered = 1, missed = 1),
        )

        return CoverageReport(
                name,
                totalStats,
                allFiles
        )
    }
}