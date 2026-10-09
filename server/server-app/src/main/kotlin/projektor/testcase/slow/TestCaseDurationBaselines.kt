package projektor.testcase.slow

import java.math.BigDecimal

data class TestCaseDurationBaselines(
    // Number of earlier runs the baselines were calculated from
    val runCount: Int,
    val baselines: List<TestCaseDurationBaseline>,
)

// Baseline for a test case in the requested run, identified by its suite and case index in that run
data class TestCaseDurationBaseline(
    val testSuiteIdx: Int,
    val testCaseIdx: Int,
    val medianDuration: BigDecimal,
    val sampleCount: Int,
)
