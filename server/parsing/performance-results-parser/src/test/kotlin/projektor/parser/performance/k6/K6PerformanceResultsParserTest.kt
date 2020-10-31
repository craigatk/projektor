package projektor.parser.performance.k6

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.performance.PerformanceResultsLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class K6PerformanceResultsParserTest : StringSpec({
    "should parse k6 results" {
        val k6PerformanceResultsParser = K6PerformanceResultsParser()

        val k6ResultsStr = PerformanceResultsLoader().k6tGetFailedTestCasesLarge()

        val k6Results = k6PerformanceResultsParser.parseResults(k6ResultsStr)

        expectThat(k6Results.metrics.requestDurationStats) {
            get { average }.isEqualTo(BigDecimal("32.683954585721736"))
            get { maximum }.isEqualTo(BigDecimal("382.6701"))
            get { p95 }.isEqualTo(BigDecimal("60.027325000000005"))
        }

        expectThat(k6Results.metrics.iterations) {
            get { count }.isEqualTo(7675)
            get { rate }.isEqualTo(BigDecimal("496.0022509389969"))
        }
    }
})
