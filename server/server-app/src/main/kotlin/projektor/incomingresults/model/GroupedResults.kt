package projektor.incomingresults.model

import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.quality.CodeQualityReport
import java.math.BigDecimal

data class GroupedResults(
    val groupedTestSuites: List<GroupedTestSuites>,
    val performanceResults: List<PerformanceResult>,
    val metadata: ResultsMetadata?,
    val wallClockDuration: BigDecimal?,
    val coverageFiles: List<CoverageFilePayload>?,
    val codeQualityReports: List<CodeQualityReport>?
)
