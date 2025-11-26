package projektor.parser.coverage.go

import projektor.parser.coverage.go.model.GoCoverageBlock
import projektor.parser.coverage.go.model.GoCoverageFile
import projektor.parser.coverage.go.model.GoCoverageReport

object GoCoverageReportParser {
    private val MODE_PATTERN = Regex("""^mode:\s*(\w+)\s*$""")
    private val COVERAGE_LINE_PATTERN = Regex("""^(.+):(\d+)\.(\d+),(\d+)\.(\d+)\s+(\d+)\s+(\d+)$""")

    fun isGoCoverageReport(report: String): Boolean {
        val firstLine = report.lineSequence().firstOrNull()?.trim() ?: return false
        return MODE_PATTERN.matches(firstLine)
    }

    fun parseReport(report: String): GoCoverageReport {
        val lines = report.lines()
        if (lines.isEmpty()) {
            throw GoCoverageParseException("Empty coverage report")
        }

        val modeMatch =
            MODE_PATTERN.matchEntire(lines[0].trim())
                ?: throw GoCoverageParseException("Invalid mode line: ${lines[0]}")

        val mode = modeMatch.groupValues[1]

        val blocksByFile = mutableMapOf<String, MutableList<GoCoverageBlock>>()

        lines.drop(1)
            .filter { it.isNotBlank() }
            .forEach { line ->
                val match = COVERAGE_LINE_PATTERN.matchEntire(line.trim())
                if (match != null) {
                    val filePath = match.groupValues[1]
                    val block =
                        GoCoverageBlock(
                            startLine = match.groupValues[2].toInt(),
                            startCol = match.groupValues[3].toInt(),
                            endLine = match.groupValues[4].toInt(),
                            endCol = match.groupValues[5].toInt(),
                            numStatements = match.groupValues[6].toInt(),
                            count = match.groupValues[7].toInt(),
                        )
                    blocksByFile.getOrPut(filePath) { mutableListOf() }.add(block)
                }
            }

        val files =
            blocksByFile.map { (path, blocks) ->
                GoCoverageFile(filePath = path, blocks = blocks)
            }

        return GoCoverageReport(mode = mode, files = files)
    }
}

class GoCoverageParseException(message: String) : RuntimeException(message)
