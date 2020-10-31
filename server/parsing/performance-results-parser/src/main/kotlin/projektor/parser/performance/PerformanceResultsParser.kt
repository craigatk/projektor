package projektor.parser.performance

import projektor.parser.performance.k6.K6PerformanceResultsParser
import projektor.parser.performance.model.PerformanceResults
import projektor.parser.performance.model.PerformanceStats
import projektor.parser.performance.model.RequestStats

class PerformanceResultsParser {
    private val k6PerformanceResultsParser = K6PerformanceResultsParser()

    fun parseResults(resultsStr: String): PerformanceResults? =
        try {
            val k6Results = k6PerformanceResultsParser.parseResults(resultsStr)

            PerformanceResults(
                requestStats = RequestStats(
                    ratePerSecond = k6Results.metrics.iterations.rate,
                    count = k6Results.metrics.iterations.count
                ),
                performanceStats = PerformanceStats(
                    average = k6Results.metrics.requestDurationStats.average,
                    maximum = k6Results.metrics.requestDurationStats.maximum,
                    p95 = k6Results.metrics.requestDurationStats.p95
                )
            )
        } catch (e: Exception) {
            null
        }
}
