package projektor.incomingresults.model

import java.math.BigDecimal
import projektor.server.api.performance.PerformanceResult as PerformanceResultApi

data class PerformanceResult(
    val name: String,
    val requestsPerSecond: BigDecimal,
    val requestCount: Long,
    val average: BigDecimal,
    val maximum: BigDecimal,
    val p95: BigDecimal
) {

    fun toApi(): PerformanceResultApi =
        PerformanceResultApi(
            name = name,
            requestsPerSecond = requestsPerSecond,
            requestCount = requestCount,
            average = average,
            maximum = maximum,
            p95 = p95
        )
}
