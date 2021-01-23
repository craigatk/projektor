package projektor.parser.coverage

import projektor.parser.coverage.model.CoverageReport

sealed class CoverageParseResult {
    class Success(val coverageReport: CoverageReport): CoverageParseResult()
    class Failure(val e: Exception?): CoverageParseResult()
}