package projektor.notification.badge

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class SvgCoverageBadgeCreatorSpec : StringSpec() {
    init {
        forAll(
            row(BigDecimal("90.25"), "90%", CoverageLevel.GOOD.fillColor),
            row(BigDecimal("95.75"), "96%", CoverageLevel.GOOD.fillColor),
            row(BigDecimal("100.00"), "100%", CoverageLevel.GOOD.fillColor),
            row(BigDecimal("0.00"), "0%", CoverageLevel.TERRIBLE.fillColor)
        ) { coveredPercentage, expectedBadgePercentage, expectedFillColor ->
            "should create coverage badge for covered percentage $coveredPercentage" {
                val svgCoverageBadgeCreator = SvgCoverageBadgeCreator("coverage.template.test")

                val badge = svgCoverageBadgeCreator.createBadge(coveredPercentage)

                expectThat(badge).isEqualTo("<coverage><percentage>$expectedBadgePercentage</percentage><fill>$expectedFillColor</fill></coverage>")
            }
        }
    }
}
