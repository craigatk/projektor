package projektor.parser.coverage.payload

data class CoverageFilePayload(
    val reportContents: String,
    val baseDirectoryPath: String? = null,
)
