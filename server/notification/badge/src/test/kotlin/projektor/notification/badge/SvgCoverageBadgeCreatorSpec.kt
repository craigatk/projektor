package projektor.notification.badge

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class SvgCoverageBadgeCreatorSpec : StringSpec() {
    init {
        "should create coverage badge" {
            val svgCoverageBadgeCreator = SvgCoverageBadgeCreator("coverage.template.test")

            val badge = svgCoverageBadgeCreator.createBadge(BigDecimal("90.00"))

            expectThat(badge).isEqualTo("<coverage><percentage>90.00%</percentage><fill>#4c1</fill></coverage>")
        }
    }
}
