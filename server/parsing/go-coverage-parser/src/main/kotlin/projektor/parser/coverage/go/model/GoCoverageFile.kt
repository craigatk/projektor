package projektor.parser.coverage.go.model

data class GoCoverageFile(
    val filePath: String,
    val blocks: List<GoCoverageBlock>,
) {
    val fileName: String
        get() = filePath.substringAfterLast("/")

    val directoryName: String
        get() = filePath.substringBeforeLast("/", "")

    val coveredLines: Set<Int>
        get() =
            blocks.filter { it.isCovered }
                .flatMap { block -> (block.startLine..block.endLine) }
                .toSet()

    val missedLines: Set<Int>
        get() {
            val allLines = blocks.flatMap { block -> (block.startLine..block.endLine) }.toSet()
            return allLines - coveredLines
        }

    val partialLines: Set<Int>
        get() {
            val lineStatus = mutableMapOf<Int, MutableSet<Boolean>>()
            blocks.forEach { block ->
                (block.startLine..block.endLine).forEach { line ->
                    lineStatus.getOrPut(line) { mutableSetOf() }.add(block.isCovered)
                }
            }
            return lineStatus.filter { it.value.size > 1 }.keys
        }

    val totalStatements: Int
        get() = blocks.sumOf { it.numStatements }

    val coveredStatements: Int
        get() = blocks.filter { it.isCovered }.sumOf { it.numStatements }

    val missedStatements: Int
        get() = blocks.filter { !it.isCovered }.sumOf { it.numStatements }
}
