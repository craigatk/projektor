package projektor.testcase.cluster

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.ai.analysis.TestFailureAnalysis
import projektor.incomingresults.randomPublicId
import projektor.testcase.TestCaseDatabaseRepository
import projektor.testcase.TestCaseService
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.util.concurrent.atomic.AtomicInteger

class FailureClusterServiceTest : DatabaseRepositoryTestCase() {
    private class CountingAnalyzer : AITestFailureAnalyzer {
        val analyzedOutputs = mutableListOf<String>()
        val callCount = AtomicInteger()

        override suspend fun analyzeTestFailure(testOutput: String): TestFailureAnalysis {
            callCount.incrementAndGet()
            synchronized(analyzedOutputs) { analyzedOutputs.add(testOutput) }
            return TestFailureAnalysis("The database is not reachable", 1)
        }
    }

    private val dbTestNames = listOf("db1", "db2", "db3")

    private fun createClusteredTestRun() =
        randomPublicId().also { publicId ->
            val testRun =
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData("testSuite1", listOf("passing1"), listOf("db1", "db2", "other1"), listOf()),
                        TestSuiteData("testSuite2", listOf(), listOf("db3"), listOf("skipped1")),
                    ),
                )
            setFailureText(dslContext, testRun, dbTestNames, CONNECTION_REFUSED_FAILURE_TEXT)
        }

    @Test
    fun `should cluster failed test cases by root cause`() {
        val testCaseService = TestCaseService(TestCaseDatabaseRepository(dslContext), null, null)
        val publicId = createClusteredTestRun()

        val failureClusters = runBlocking { testCaseService.fetchFailureClusters(publicId) }

        expectThat(failureClusters.totalFailedTestCount).isEqualTo(4)
        expectThat(failureClusters.clusters).hasSize(2)

        val dbCluster = failureClusters.clusters[0]
        expectThat(dbCluster.title).isEqualTo("ConnectException in DbSetup")
        expectThat(dbCluster.testCaseCount).isEqualTo(3)
        expectThat(dbCluster.testCases.map { it.fullName.substringAfterLast('.') }).isEqualTo(dbTestNames)
        expectThat(failureClusters.clusters[1].testCaseCount).isEqualTo(1)
    }

    @Test
    fun `should analyze one representative per cluster and reuse it for the cluster's test cases`() {
        val analyzer = CountingAnalyzer()
        val testCaseService = TestCaseService(TestCaseDatabaseRepository(dslContext), null, analyzer)
        val publicId = createClusteredTestRun()

        val clusterKey = runBlocking { testCaseService.fetchFailureClusters(publicId) }.clusters[0].key

        val clusterAnalysis = runBlocking { testCaseService.analyzeFailureCluster(publicId, clusterKey) }
        expectThat(clusterAnalysis?.analysis).isEqualTo("The database is not reachable")

        // db2 and db3 are in the same cluster, so they reuse the representative's analysis
        runBlocking {
            testCaseService.analyzeTestCaseFailure(publicId, 1, 3)
            testCaseService.analyzeTestCaseFailure(publicId, 2, 1)
        }
        expectThat(analyzer.callCount.get()).isEqualTo(1)
        expectThat(analyzer.analyzedOutputs.toList()).isEqualTo(listOf(CONNECTION_REFUSED_FAILURE_TEXT))

        // "other1" has a different root cause, so it gets its own analysis
        runBlocking { testCaseService.analyzeTestCaseFailure(publicId, 1, 4) }
        expectThat(analyzer.callCount.get()).isEqualTo(2)
    }

    @Test
    fun `should return null when analyzing an unknown cluster`() {
        val analyzer = CountingAnalyzer()
        val testCaseService = TestCaseService(TestCaseDatabaseRepository(dslContext), null, analyzer)
        val publicId = createClusteredTestRun()

        expectThat(runBlocking { testCaseService.analyzeFailureCluster(publicId, "doesnotexist") }).isNull()
        expectThat(analyzer.callCount.get()).isEqualTo(0)
    }
}
