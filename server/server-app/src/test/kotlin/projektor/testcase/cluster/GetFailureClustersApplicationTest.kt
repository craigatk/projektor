package projektor.testcase.cluster

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.failure.FailureClusters
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class GetFailureClustersApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch failure clusters for test run`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            val testRun =
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData("testSuite1", listOf("passing1"), listOf("db1", "db2"), listOf()),
                        TestSuiteData("testSuite2", listOf(), listOf("db3", "other1"), listOf()),
                    ),
                )
            setFailureText(dslContext, testRun, listOf("db1", "db2", "db3"), CONNECTION_REFUSED_FAILURE_TEXT)

            val response = client.get("/run/${publicId.id}/cases/failed/clusters")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val failureClusters: FailureClusters = objectMapper.readValue(response.bodyAsText())

            expectThat(failureClusters.totalFailedTestCount).isEqualTo(4)
            expectThat(failureClusters.clusters).hasSize(2)
            expectThat(failureClusters.clusters[0]) {
                get { title }.isEqualTo("ConnectException in DbSetup")
                get { testCaseCount }.isEqualTo(3)
                get { representative.testSuiteIdx }.isEqualTo(1)
                get { representative.testCaseIdx }.isEqualTo(2)
            }
        }

    @Test
    fun `when no AI config set cluster analysis should return 204`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(TestSuiteData("testSuite1", listOf(), listOf("failing1"), listOf())),
            )

            val clustersResponse = client.get("/run/${publicId.id}/cases/failed/clusters")
            val clusterKey = objectMapper.readValue<FailureClusters>(clustersResponse.bodyAsText()).clusters[0].key

            val response = client.get("/run/${publicId.id}/cases/failed/clusters/$clusterKey/analysis")

            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
