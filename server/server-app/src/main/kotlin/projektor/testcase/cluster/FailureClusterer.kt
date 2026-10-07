package projektor.testcase.cluster

import projektor.server.api.TestCase
import projektor.server.api.failure.FailureCluster
import projektor.server.api.failure.FailureClusterTestCase
import projektor.server.api.failure.FailureClusters

object FailureClusterer {
    /**
     * Groups failed test cases by [FailureSignature], largest cluster first.
     * The first failed test case in each cluster is its representative.
     */
    fun cluster(failedTestCases: List<TestCase>): FailureClusters {
        val clusters =
            failedTestCases
                .groupBy { FailureSignature.fromTestCase(it) }
                .map { (signature, testCases) -> toCluster(signature, testCases) }
                .sortedByDescending { it.testCaseCount }

        return FailureClusters(totalFailedTestCount = failedTestCases.size, clusters = clusters)
    }

    private fun toCluster(
        signature: FailureSignature,
        testCases: List<TestCase>,
    ): FailureCluster {
        val clusterTestCases = testCases.map { FailureClusterTestCase(it.testSuiteIdx, it.idx, it.fullName) }

        return FailureCluster(
            key = signature.key,
            title = signature.title,
            failureType = signature.failureType,
            normalizedMessage = signature.normalizedMessage,
            location = signature.location?.display,
            testCaseCount = testCases.size,
            representative = clusterTestCases.first(),
            testCases = clusterTestCases,
        )
    }
}
