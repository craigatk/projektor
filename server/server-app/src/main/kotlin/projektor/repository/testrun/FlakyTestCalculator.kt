package projektor.repository.testrun

import projektor.server.api.TestCase
import projektor.server.api.repository.RepositoryFlakyTest
import projektor.server.api.repository.RepositoryFlakyTests

class FlakyTestCalculator {
    fun calculateFlakyTests(failingTestCases: List<TestCase>, flakyFailureThreshold: Int): RepositoryFlakyTests {
        val testCaseGroups = failingTestCases.groupBy { it.fullName }

        val flakyTestCaseGroups = testCaseGroups.filter { it.value.size >= flakyFailureThreshold }

        val flakyTestCases = flakyTestCaseGroups.map { flakyTestCaseGroup ->
            val latestTestCase = flakyTestCaseGroup.value.maxByOrNull { it.createdTimestamp }!!

            RepositoryFlakyTest(
                    testCase = flakyTestCaseGroup.value.first(),
                    failureCount = flakyTestCaseGroup.value.size,
                    latestPublicId = latestTestCase.publicId,
                    latestCreatedTimestamp = latestTestCase.createdTimestamp
            )
        }

        return RepositoryFlakyTests(flakyTestCases)
    }
}