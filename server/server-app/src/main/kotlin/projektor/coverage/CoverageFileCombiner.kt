package projektor.coverage

import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat

fun combineCoverageFiles(existingCoverageFiles: List<CoverageFile>, incomingCoverageFiles: List<CoverageFile>): List<CoverageFile> {
    val existingFilePaths = existingCoverageFiles.mapNotNull { it.filePath }

    val coverageFilesToAdd = incomingCoverageFiles.filter { !existingFilePaths.contains(it.filePath) }

    val updatedCoverageFiles = existingCoverageFiles.map { existingCoverageFile ->
        val matchedIncomingCoverageFile = incomingCoverageFiles.find { it.filePath == existingCoverageFile.filePath }

        if (matchedIncomingCoverageFile != null) {
            val totalLines = existingCoverageFile.stats.lineStat.total
            val missedLines: Array<Int> = existingCoverageFile.missedLines.intersect(matchedIncomingCoverageFile.missedLines.toList()).toTypedArray()

            val newLineStat = CoverageStat(
                covered = totalLines - missedLines.size,
                missed = missedLines.size
            )

            existingCoverageFile.copy(
                missedLines = missedLines,
                stats = existingCoverageFile.stats.copy(
                    lineStat = newLineStat
                )
            )
        } else {
            existingCoverageFile
        }
    }

    return updatedCoverageFiles + coverageFilesToAdd
}
