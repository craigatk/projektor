package projektor.server.api.coverage

data class CoverageFile(
    val fileName: String,
    val directoryName: String,
    val missedLines: List<Int>,
    val partialLines: List<Int>,
    val stats: CoverageStats
)
