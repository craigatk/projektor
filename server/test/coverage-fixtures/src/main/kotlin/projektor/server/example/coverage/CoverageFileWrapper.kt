package projektor.server.example.coverage

import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser

object CoverageFileWrapper {
    private val coveragePayloadParser = CoveragePayloadParser()

    fun createCoverageFilePayload(payload: String, baseDirectoryPath: String?): String =
        coveragePayloadParser.serializeCoverageFilePayload(
            CoverageFilePayload(
                reportContents = payload,
                baseDirectoryPath = baseDirectoryPath
            )
        )
}
