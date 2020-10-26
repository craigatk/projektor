package projektor.server.api.coverage

data class CoverageFile(
    val fileName: String,
    val directoryName: String,
    val missedLines: Array<Int>,
    val partialLines: Array<Int>,
    val stats: CoverageStats
)
