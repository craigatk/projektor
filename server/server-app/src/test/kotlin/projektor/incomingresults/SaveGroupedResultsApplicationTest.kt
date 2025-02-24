package projektor.incomingresults

import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.server.api.results.ResultsProcessingStatus
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class SaveGroupedResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save grouped test results`() =
        projektorTestApplication {
            val requestBody = GroupedResultsXmlLoader().passingGroupedResults()

            val response = client.postGroupedResultsJSON(requestBody)

            val (publicId, testRun) = waitForTestRunSaveToComplete(response)

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(3)

            expectThat(testSuites.find { it.idx == 1 }).isNotNull()
            expectThat(testSuites.find { it.idx == 2 }).isNotNull()
            expectThat(testSuites.find { it.idx == 3 }).isNotNull()

            val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
            expectThat(testSuiteGroups)
                .hasSize(2)

            val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
            assertNotNull(testSuiteGroup1)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(2)

            val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
            assertNotNull(testSuiteGroup2)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)

            await until { resultsProcessingDao.fetchOneByPublicId(publicId.id).status == ResultsProcessingStatus.SUCCESS.name }
        }
}
