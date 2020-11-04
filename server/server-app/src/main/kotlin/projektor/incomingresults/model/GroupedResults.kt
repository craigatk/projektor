package projektor.incomingresults.model

import java.math.BigDecimal

data class GroupedResults(
    val groupedTestSuites: List<GroupedTestSuites>,
    val performanceResults: List<PerformanceResult>,
    val metadata: ResultsMetadata?,
    val wallClockDuration: BigDecimal?
)
