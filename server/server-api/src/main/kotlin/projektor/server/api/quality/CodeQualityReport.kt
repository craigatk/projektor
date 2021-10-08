package projektor.server.api.quality

data class CodeQualityReport(
    val contents: String,
    val fileName: String,
    val groupName: String?
)
