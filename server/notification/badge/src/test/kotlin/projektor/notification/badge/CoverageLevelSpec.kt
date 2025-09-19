package projektor.notification.badge

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class CoverageLevelSpec : StringSpec({
    "should calculate coverage level" {
        forAll(
            row(BigDecimal("95.00"), CoverageLevel.GOOD),
            row(BigDecimal("85.00"), CoverageLevel.OK),
            row(BigDecimal("75.00"), CoverageLevel.POOR),
            row(BigDecimal("50.00"), CoverageLevel.TERRIBLE),
        ) { coveredPercentage, expectedCoverageLevel ->
            {
                expectThat(CoverageLevel.of(coveredPercentage)).isEqualTo(expectedCoverageLevel)
            }
        }
    }
})
