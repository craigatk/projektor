package projektor.parser.coverage.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class CoverageReportStatSpec : StringSpec({
    "should calculate total and percent covered"() {
        forAll(
            row(50, 50, 100, BigDecimal("50.00")),
            row(33, 66, 99, BigDecimal("33.33")),
            row(60, 0, 60, BigDecimal("100.00")),
            row(0, 0, 0, BigDecimal.ZERO)
        ) { covered, missed, expectedTotal, expectedPercentCovered ->
            val coverageStat = CoverageReportStat(covered = covered, missed = missed)

            expectThat(coverageStat) {
                get { total }.isEqualTo(expectedTotal)
                get { percentCovered }.isEqualTo(expectedPercentCovered)
            }
        }
    }
})
