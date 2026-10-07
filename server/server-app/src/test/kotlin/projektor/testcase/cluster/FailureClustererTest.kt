package projektor.testcase.cluster

import org.junit.jupiter.api.Test
import projektor.server.api.TestCase
import projektor.server.api.TestFailure
import projektor.server.api.failure.FailureCluster
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.time.LocalDateTime

class FailureClustererTest {
    @Test
    fun `should group failures by root cause with the largest cluster first`() {
        val connectionFailure = "java.net.ConnectException: Connection refused\n\tat com.acme.DbSetup.connect(DbSetup.kt:12)"

        val failedTestCases =
            listOf(
                failedTestCase(
                    1,
                    1,
                    "assertion",
                    "org.opentest4j.AssertionFailedError: expected: <1> but was: <2>\n\tat com.acme.MathTest.adds(MathTest.kt:9)",
                ),
                failedTestCase(1, 2, "db1", connectionFailure),
                failedTestCase(2, 1, "db2", connectionFailure),
                failedTestCase(3, 4, "db3", connectionFailure),
            )

        val failureClusters = FailureClusterer.cluster(failedTestCases)

        expectThat(failureClusters.totalFailedTestCount).isEqualTo(4)
        expectThat(failureClusters.clusters).hasSize(2)
        expectThat(
            failureClusters.clusters,
        ).map(FailureCluster::title).containsExactly("ConnectException in DbSetup", "AssertionFailedError in MathTest")

        val dbCluster = failureClusters.clusters[0]
        expectThat(dbCluster.testCaseCount).isEqualTo(3)
        expectThat(dbCluster.location).isEqualTo("DbSetup.connect")
        expectThat(dbCluster.representative.testSuiteIdx).isEqualTo(1)
        expectThat(dbCluster.representative.testCaseIdx).isEqualTo(2)
        expectThat(dbCluster.testCases).map { it.fullName }.containsExactly("com.acme.Test.db1", "com.acme.Test.db2", "com.acme.Test.db3")
    }

    private fun failedTestCase(
        testSuiteIdx: Int,
        idx: Int,
        name: String,
        failureText: String,
    ) = TestCase(
        idx = idx,
        testSuiteIdx = testSuiteIdx,
        name = name,
        packageName = "com.acme",
        testSuiteName = "Test",
        className = "Test",
        fileName = null,
        duration = null,
        passed = false,
        skipped = false,
        hasSystemOutTestCase = false,
        hasSystemErrTestCase = false,
        hasSystemOutTestSuite = false,
        hasSystemErrTestSuite = false,
        publicId = "ABC123",
        createdTimestamp = LocalDateTime.now(),
        failure = TestFailure(failureText.lines().first(), null, failureText),
    )
}
