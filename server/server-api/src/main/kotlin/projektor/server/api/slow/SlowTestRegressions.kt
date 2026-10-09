package projektor.server.api.slow

import projektor.server.api.TestCase
import java.math.BigDecimal

data class SlowTestRegressions(
    val thresholdPercent: Int,
    // Number of earlier CI runs from the same repo, project, and branch used as the baseline
    val baselineRunCount: Int,
    // Largest duration increase first
    val regressions: List<SlowTestRegression>,
)

data class SlowTestRegression(
    val testCase: TestCase,
    // Median duration of the test case's passing runs in the baseline
    val baselineDuration: BigDecimal,
    // Number of baseline runs the test case passed in
    val baselineSampleCount: Int,
    val durationIncrease: BigDecimal,
    val increasePercent: BigDecimal,
)
