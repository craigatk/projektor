package projektor.repository.testrun

import projektor.parser.coverage.MathUtil.calculatePercentage
import projektor.server.api.TestCase
import projektor.server.api.repository.RepositoryFlakyTest

class FlakyTestCalculator {
    fun calculateFlakyTests(failingTestCases: List<TestCase>, flakyFailureThreshold: Int, testRunCount: Long): List<RepositoryFlakyTest> {
        val testCaseGroups = failingTestCases.groupBy { it.fullName }

        val flakyTestCaseGroups = testCaseGroups.filter { it.value.size >= flakyFailureThreshold }

        val flakyTestCases = flakyTestCaseGroups.map { flakyTestCaseGroup ->
            val latestTestCase = flakyTestCaseGroup.value.maxByOrNull { it.createdTimestamp }!!

            val failurePercentage = calculatePercentage(
                flakyTestCaseGroup.value.size.toBigDecimal(),
                testRunCount.toBigDecimal()
            )

            RepositoryFlakyTest(
                testCase = flakyTestCaseGroup.value.first(),
                failureCount = flakyTestCaseGroup.value.size,
                failurePercentage = failurePercentage,
                latestPublicId = latestTestCase.publicId,
                latestCreatedTimestamp = latestTestCase.createdTimestamp
            )
        }

        return flakyTestCases
    }
}
