package projektor.testcase.slow

import projektor.server.api.TestCase
import projektor.server.api.slow.SlowTestRegression
import projektor.server.api.slow.SlowTestRegressions
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Flags test cases whose duration in this run is more than [thresholdPercent] slower than their baseline.
 *
 * To keep noise out, a test case needs at least [minBaselineSamples] baseline runs, and has to be
 * at least [minDurationIncrease] seconds slower (so e.g. a 2ms test taking 6ms isn't a "200% regression").
 */
object SlowTestRegressionDetector {
    const val DEFAULT_THRESHOLD_PERCENT = 50
    const val DEFAULT_MIN_BASELINE_SAMPLES = 3
    val DEFAULT_MIN_DURATION_INCREASE = BigDecimal("0.1")

    fun detect(
        testCases: List<TestCase>,
        baselines: TestCaseDurationBaselines,
        thresholdPercent: Int = DEFAULT_THRESHOLD_PERCENT,
        minBaselineSamples: Int = DEFAULT_MIN_BASELINE_SAMPLES,
        minDurationIncrease: BigDecimal = DEFAULT_MIN_DURATION_INCREASE,
    ): SlowTestRegressions {
        val baselinesByTestCase = baselines.baselines.associateBy { it.testSuiteIdx to it.testCaseIdx }

        val regressions =
            testCases
                .filterNot { it.skipped }
                .mapNotNull { testCase ->
                    val duration = testCase.duration ?: return@mapNotNull null
                    val baseline =
                        baselinesByTestCase[testCase.testSuiteIdx to testCase.idx]
                            ?.takeIf { it.sampleCount >= minBaselineSamples && it.medianDuration.signum() > 0 }
                            ?: return@mapNotNull null

                    val durationIncrease = duration - baseline.medianDuration
                    val increasePercent =
                        durationIncrease
                            .multiply(BigDecimal(100))
                            .divide(baseline.medianDuration, 1, RoundingMode.HALF_UP)

                    if (durationIncrease >= minDurationIncrease && increasePercent > BigDecimal(thresholdPercent)) {
                        SlowTestRegression(
                            testCase = testCase,
                            baselineDuration = baseline.medianDuration.setScale(3, RoundingMode.HALF_UP),
                            baselineSampleCount = baseline.sampleCount,
                            durationIncrease = durationIncrease.setScale(3, RoundingMode.HALF_UP),
                            increasePercent = increasePercent,
                        )
                    } else {
                        null
                    }
                }
                .sortedByDescending { it.durationIncrease }

        return SlowTestRegressions(thresholdPercent, baselines.runCount, regressions)
    }
}
