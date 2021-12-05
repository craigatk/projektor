package projektor.repository.testrun

import org.junit.jupiter.api.Test
import projektor.server.api.TestCase
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.time.LocalDateTime

class FlakyTestCalculatorTest {
    @Test
    fun `when one flaky test failing in three runs should calculate flaky tests`() {
        val packageName = "dev.projektor"
        val className = "MyFlakyTests"
        val testCaseName = "shouldSometimesPass"
        val oldestTestCase = createTestCase(
            packageName,
            className,
            testCaseName,
            "oldest-public-id",
            LocalDateTime.now().minusDays(2)
        )
        val middleTestCase = createTestCase(
            packageName,
            className,
            testCaseName,
            "middle-public-id",
            LocalDateTime.now().minusDays(1)
        )
        val newestTestCase = createTestCase(
            packageName,
            className,
            testCaseName,
            "newest-public-id",
            LocalDateTime.now()
        )

        val flakyTests = FlakyTestCalculator().calculateFlakyTests(
            listOf(oldestTestCase, middleTestCase, newestTestCase),
            3,
            6
        )

        expectThat(flakyTests).hasSize(1)

        expectThat(flakyTests[0]) {
            get { failureCount }.isEqualTo(3)
            get { failurePercentage }.isEqualTo(BigDecimal("50.00"))
            get { firstTestCase.publicId }.isEqualTo(oldestTestCase.publicId)
            get { firstTestCase.createdTimestamp }.isEqualTo(oldestTestCase.createdTimestamp)
            get { latestTestCase.publicId }.isEqualTo(newestTestCase.publicId)
            get { latestTestCase.createdTimestamp }.isEqualTo(newestTestCase.createdTimestamp)
        }
    }

    @Test
    fun `when one test under threshold and another over threshold should calculate flaky tests`() {
        val packageName = "dev.projektor"
        val className = "MyFlakyTests"

        val notFlakyEnoughTestCases = (1..2).map { idx ->
            createTestCase(
                packageName,
                className,
                "notFlakyEnough",
                "public-id-$idx",
                LocalDateTime.now()
            )
        }

        val flakyTestCases = (1..3).map { idx ->
            createTestCase(
                packageName,
                className,
                "soFlaky",
                "public-id-$idx",
                LocalDateTime.now()
            )
        }

        val flakyTests = FlakyTestCalculator().calculateFlakyTests(
            notFlakyEnoughTestCases + flakyTestCases,
            3,
            3
        )

        expectThat(flakyTests).hasSize(1)

        expectThat(flakyTests[0]) {
            get { testCase }.get { name }.isEqualTo("soFlaky")
            get { failurePercentage }.isEqualTo(BigDecimal("100.00"))
        }
    }

    @Test
    fun `when multiple flaky tests should group them by test case name`() {
        val packageName = "dev.projektor"
        val className = "MyFlakyTests"

        val flakyTestCases1 = (1..4).map { idx ->
            createTestCase(
                packageName,
                className,
                "flaky-1",
                "public-id-$idx",
                LocalDateTime.now().plusDays(idx.toLong())
            )
        }

        val flakyTestCases2 = (1..3).map { idx ->
            createTestCase(
                packageName,
                className,
                "soFlaky",
                "public-id-$idx",
                LocalDateTime.now().plusDays(idx.toLong())
            )
        }

        val flakyTests = FlakyTestCalculator().calculateFlakyTests(
            flakyTestCases1 + flakyTestCases2,
            3,
            6
        )

        expectThat(flakyTests) {
            any {
                get { testCase }.get { name }.isEqualTo("flaky-1")
                get { latestTestCase.publicId }.isEqualTo("public-id-4")
            }
            any {
                get { testCase }.get { name }.isEqualTo("soFlaky")
                get { latestTestCase.publicId }.isEqualTo("public-id-3")
            }
        }
    }

    private fun createTestCase(packageName: String, className: String, testCaseName: String, publicId: String, createdTimeStamp: LocalDateTime) = TestCase(
        idx = 1,
        testSuiteIdx = 1,
        name = testCaseName,
        testSuiteName = null,
        className = className,
        packageName = packageName,
        fileName = null,
        duration = BigDecimal.ONE,
        passed = false,
        skipped = false,
        hasSystemOutTestCase = false,
        hasSystemErrTestCase = false,
        hasSystemOutTestSuite = false,
        hasSystemErrTestSuite = false,
        publicId = publicId,
        createdTimestamp = createdTimeStamp,
        failure = null
    )
}
