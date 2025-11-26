package projektor.parser.coverage.go.model

data class GoCoverageReport(
    val mode: String,
    val files: List<GoCoverageFile>,
)
