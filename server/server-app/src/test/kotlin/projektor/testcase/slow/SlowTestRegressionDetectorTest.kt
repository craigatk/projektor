package projektor.testcase.slow

import org.junit.jupiter.api.Test
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.math.BigDecimal
import java.time.LocalDateTime

class SlowTestRegressionDetectorTest {
    @Test
    fun `should flag test cases slower than their baseline by more than the threshold`() {
        val testCases =
            listOf(
                testCase(1, 1, "2.0"),
                testCase(1, 2, "1.4"),
                testCase(1, 3, "1.5"),
                testCase(2, 1, "0.9"),
            )
        val baselines =
            baselines(
                baseline(1, 1, "1.0"),
                baseline(1, 2, "1.0"),
                baseline(1, 3, "1.0"),
                baseline(2, 1, "1.0"),
            )

        val result = SlowTestRegressionDetector.detect(testCases, baselines, thresholdPercent = 40)

        expectThat(result.regressions).hasSize(2)
        expectThat(result.regressions[0]) {
            get { testCase.name }.isEqualTo("test-1-1")
            get { baselineDuration }.isEqualTo(BigDecimal("1.000"))
            get { durationIncrease }.isEqualTo(BigDecimal("1.000"))
            get { increasePercent }.isEqualTo(BigDecimal("100.0"))
            get { baselineSampleCount }.isEqualTo(5)
        }
        expectThat(result.regressions[1].testCase.name).isEqualTo("test-1-3")
        expectThat(result.thresholdPercent).isEqualTo(40)
        expectThat(result.baselineRunCount).isEqualTo(10)
    }

    @Test
    fun `should not flag a test case that is exactly at the threshold`() {
        val result =
            SlowTestRegressionDetector.detect(
                listOf(testCase(1, 1, "1.5")),
                baselines(baseline(1, 1, "1.0")),
                thresholdPercent = 50,
            )

        expectThat(result.regressions).isEmpty()
    }

    @Test
    fun `should order regressions by largest duration increase`() {
        val testCases = listOf(testCase(1, 1, "0.5"), testCase(1, 2, "30.0"), testCase(1, 3, "4.0"))
        val baselines = baselines(baseline(1, 1, "0.1"), baseline(1, 2, "10.0"), baseline(1, 3, "1.0"))

        val result = SlowTestRegressionDetector.detect(testCases, baselines)

        expectThat(result.regressions)
            .map { it.testCase.name }
            .containsExactly("test-1-2", "test-1-3", "test-1-1")
    }

    @Test
    fun `should ignore tiny absolute increases even when the percent increase is large`() {
        val result =
            SlowTestRegressionDetector.detect(
                listOf(testCase(1, 1, "0.006")),
                baselines(baseline(1, 1, "0.002")),
            )

        expectThat(result.regressions).isEmpty()
    }

    @Test
    fun `should skip test cases without enough baseline samples`() {
        val result =
            SlowTestRegressionDetector.detect(
                listOf(testCase(1, 1, "5.0"), testCase(1, 2, "5.0")),
                baselines(baseline(1, 1, "1.0", sampleCount = 2), baseline(1, 2, "1.0", sampleCount = 3)),
            )

        expectThat(result.regressions).map { it.testCase.name }.containsExactly("test-1-2")
    }

    @Test
    fun `should skip test cases that were skipped, have no duration, have no baseline, or have a zero baseline`() {
        val testCases =
            listOf(
                testCase(1, 1, "5.0", skipped = true),
                testCase(1, 2, null),
                testCase(1, 3, "5.0"),
                testCase(1, 4, "5.0"),
            )
        val baselines = baselines(baseline(1, 1, "1.0"), baseline(1, 2, "1.0"), baseline(1, 4, "0"))

        val result = SlowTestRegressionDetector.detect(testCases, baselines)

        expectThat(result.regressions).isEmpty()
    }

    private fun baselines(vararg baselines: TestCaseDurationBaseline) = TestCaseDurationBaselines(10, baselines.toList())

    private fun baseline(
        testSuiteIdx: Int,
        testCaseIdx: Int,
        medianDuration: String,
        sampleCount: Int = 5,
    ) = TestCaseDurationBaseline(testSuiteIdx, testCaseIdx, BigDecimal(medianDuration), sampleCount)

    private fun testCase(
        testSuiteIdx: Int,
        idx: Int,
        duration: String?,
        skipped: Boolean = false,
    ) = TestCase(
        idx = idx,
        testSuiteIdx = testSuiteIdx,
        name = "test-$testSuiteIdx-$idx",
        packageName = "com.acme",
        testSuiteName = "Suite",
        className = "Suite",
        fileName = null,
        duration = duration?.let(::BigDecimal),
        passed = !skipped,
        skipped = skipped,
        hasSystemOutTestCase = false,
        hasSystemErrTestCase = false,
        hasSystemOutTestSuite = false,
        hasSystemErrTestSuite = false,
        publicId = "RUN",
        createdTimestamp = LocalDateTime.now(),
        failure = null,
    )
}
