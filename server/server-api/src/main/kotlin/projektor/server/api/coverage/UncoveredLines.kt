package projektor.server.api.coverage

data class UncoveredLines(
    val testRunPublicId: String,
    val files: List<UncoveredLinesFile>,
)

data class UncoveredLinesFile(
    val groupName: String,
    val filePath: String,
    val lineStat: CoverageStat,
    val branchStat: CoverageStat,
    val missedLines: List<Int>,
    val partialLines: List<Int>,
)
