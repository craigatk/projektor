package projektor.parser.coverage.go.model

data class GoCoverageBlock(
    val startLine: Int,
    val startCol: Int,
    val endLine: Int,
    val endCol: Int,
    val numStatements: Int,
    val count: Int,
) {
    val isCovered: Boolean
        get() = count > 0
}
