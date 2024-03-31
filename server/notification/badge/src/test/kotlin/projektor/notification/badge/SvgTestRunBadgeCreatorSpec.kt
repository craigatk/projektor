package projektor.notification.badge

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

class SvgTestRunBadgeCreatorSpec : StringSpec() {
    init {
        forAll(
            row(true, "passing", BuildLabel.PASS.fillColor),
            row(false, "failing", BuildLabel.FAIL.fillColor),
        ) { passing, expectedLabel, expectedFillColor ->
            "should create test run SVG badge when passing $passing" {
                val testRunBadgeCreator = SvgTestRunBadgeCreator("test_run.template.test")

                val badge = testRunBadgeCreator.createBadge(passing)

                expectThat(badge).isEqualTo("<test_run><label>$expectedLabel</label><fill>$expectedFillColor</fill></test_run>")
            }

            "should create real badge when passing $passing" {
                val testRunBadgeCreator = SvgTestRunBadgeCreator()

                val badge = testRunBadgeCreator.createBadge(passing)

                expectThat(badge).isNotEmpty()
            }
        }
    }
}
