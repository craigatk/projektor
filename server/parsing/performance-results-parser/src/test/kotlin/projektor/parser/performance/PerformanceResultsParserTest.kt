package projektor.parser.performance

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.performance.PerformanceResultsLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal

class PerformanceResultsParserTest : StringSpec({
    "should parse k6 results" {
        val performanceResultsParser = PerformanceResultsParser()

        val k6ResultsStr = PerformanceResultsLoader().k6tGetFailedTestCasesLarge()

        val results = performanceResultsParser.parseResults(k6ResultsStr)

        expectThat(results).isNotNull().and {
            get { requestStats.count }.isEqualTo(7675)
            get { requestStats.ratePerSecond }.isEqualTo(BigDecimal("496.0022509389969"))

            get { performanceStats.average }.isEqualTo(BigDecimal("32.683954585721736"))
            get { performanceStats.maximum }.isEqualTo(BigDecimal("382.6701"))
            get { performanceStats.p95 }.isEqualTo(BigDecimal("60.027325000000005"))
        }
    }
})
