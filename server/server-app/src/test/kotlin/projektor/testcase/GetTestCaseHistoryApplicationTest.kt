package projektor.testcase

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.server.api.history.TestCaseHistory
import projektor.server.api.history.TestCaseHistoryEntry
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.map

class GetTestCaseHistoryApplicationTest : ApplicationTestCase() {
    private val passingSuite =
        listOf(TestSuiteData("projektor.HistorySuite", listOf("other", "target"), listOf(), listOf()))

    private val failingSuite =
        listOf(TestSuiteData("projektor.HistorySuite", listOf("other"), listOf("target"), listOf()))

    @Test
    fun `should return history of test case and where the current failure streak started`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            val passing1 = randomPublicId()
            val passing2 = randomPublicId()
            val failing1 = randomPublicId()
            val failing2 = randomPublicId()
            val failing3 = randomPublicId()

            testRunDBGenerator.createTestRunInRepo(passing1, passingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(passing2, passingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(failing1, failingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(failing2, failingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(failing3, failingSuite, repoName, true, null)

            val response = client.get("/run/${failing3.id}/suite/1/case/2/history")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val history = objectMapper.readValue(response.bodyAsText(), TestCaseHistory::class.java)

            expectThat(history.entries)
                .map(TestCaseHistoryEntry::publicId)
                .containsExactly(failing3.id, failing2.id, failing1.id, passing2.id, passing1.id)
            expectThat(history.entries).map(TestCaseHistoryEntry::passed).containsExactly(false, false, false, true, true)

            expectThat(history.firstFailure).isNotNull().get { publicId }.isEqualTo(failing1.id)
            expectThat(history.lastPassedBeforeFailure).isNotNull().get { publicId }.isEqualTo(passing2.id)
        }

    @Test
    fun `should only include earlier CI runs on the same branch`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            val earlierRun = randomPublicId()
            val nonCIRun = randomPublicId()
            val otherBranchRun = randomPublicId()
            val otherRepoRun = randomPublicId()
            val requestedRun = randomPublicId()
            val laterRun = randomPublicId()

            testRunDBGenerator.createTestRunInRepo(earlierRun, passingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(nonCIRun, failingSuite, repoName, false, null)
            testRunDBGenerator.createTestRunInRepo(otherBranchRun, failingSuite, repoName, true, null, "feature")
            testRunDBGenerator.createTestRunInRepo(otherRepoRun, failingSuite, "$repoName-other", true, null)
            testRunDBGenerator.createTestRunInRepo(requestedRun, passingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(laterRun, failingSuite, repoName, true, null)

            val response = client.get("/run/${requestedRun.id}/suite/1/case/2/history")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val history = objectMapper.readValue(response.bodyAsText(), TestCaseHistory::class.java)

            expectThat(history.entries)
                .map(TestCaseHistoryEntry::publicId)
                .containsExactly(requestedRun.id, earlierRun.id)
            expectThat(history.firstFailure).isNull()
        }

    @Test
    fun `should include requested run even when it was not run in CI`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            val ciRun = randomPublicId()
            val localRun = randomPublicId()

            testRunDBGenerator.createTestRunInRepo(ciRun, passingSuite, repoName, true, null)
            testRunDBGenerator.createTestRunInRepo(localRun, failingSuite, repoName, false, null)

            val response = client.get("/run/${localRun.id}/suite/1/case/2/history")

            val history = objectMapper.readValue(response.bodyAsText(), TestCaseHistory::class.java)

            expectThat(history.entries)
                .map(TestCaseHistoryEntry::publicId)
                .containsExactly(localRun.id, ciRun.id)
            expectThat(history.firstFailure).isNotNull().get { publicId }.isEqualTo(localRun.id)
        }

    @Test
    fun `should limit history to max runs`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            val publicIds: List<PublicId> = (1..5).map { randomPublicId() }
            publicIds.forEach { testRunDBGenerator.createTestRunInRepo(it, failingSuite, repoName, true, null) }

            val response = client.get("/run/${publicIds.last().id}/suite/1/case/2/history?max_runs=3")

            val history = objectMapper.readValue(response.bodyAsText(), TestCaseHistory::class.java)

            expectThat(history.entries)
                .map(TestCaseHistoryEntry::publicId)
                .containsExactly(publicIds[4].id, publicIds[3].id, publicIds[2].id)
            expectThat(history.firstFailure).isNotNull().get { publicId }.isEqualTo(publicIds[2].id)
            expectThat(history.lastPassedBeforeFailure).isNull()
        }

    @Test
    fun `should return empty history when test run has no git metadata`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            testRunDBGenerator.createTestRun(publicId, failingSuite)

            val response = client.get("/run/${publicId.id}/suite/1/case/2/history")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val history = objectMapper.readValue(response.bodyAsText(), TestCaseHistory::class.java)

            expectThat(history.entries).isEmpty()
            expectThat(history.firstFailure).isNull()
        }
}
