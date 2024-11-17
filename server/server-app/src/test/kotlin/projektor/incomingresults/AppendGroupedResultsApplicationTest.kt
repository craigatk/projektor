package projektor.incomingresults

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.statement.*
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.database.generated.tables.pojos.TestRun
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

class AppendGroupedResultsApplicationTest : ApplicationTestCase() {
    @Test
    fun `should append second grouped test results run`() =
        projektorTestApplication {
            val repoPart = RandomStringUtils.randomAlphabetic(12)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/$repoPart"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            metadata.group = "B12"

            val initialRequestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            lateinit var publicId: PublicId
            lateinit var testRun: TestRun

            val initialResponse = client.postGroupedResultsJSON(initialRequestBody)

            val initialSaveComplete = waitForTestRunSaveToComplete(initialResponse)
            publicId = initialSaveComplete.first
            testRun = initialSaveComplete.second

            expectThat(testRun) {
                get { passed }.isTrue()
                get { totalPassingCount }.isEqualTo(3)
                get { totalFailureCount }.isEqualTo(0)
                get { totalSkippedCount }.isEqualTo(0)
                get { totalTestCount }.isEqualTo(3)
            }

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(3)

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

            val secondRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), metadata)

            val secondResponse = client.postGroupedResultsJSON(secondRequestBody)

            val responseContent = secondResponse.bodyAsText()
            assertNotNull(responseContent)
            val resultsResponse: SaveResultsResponse = objectMapper.readValue(responseContent)

            val secondPublicIdResponse = resultsResponse.id
            expectThat(secondPublicIdResponse).isEqualTo(publicId.id)

            val (secondPublicId, _) = waitForTestRunSaveToComplete(secondResponse)
            expectThat(secondPublicId).isEqualTo(publicId)

            await until {
                val updatedTestRun = testRunDao.fetchOneByPublicId(publicId.id)

                !updatedTestRun.passed
            }

            val secondTestRun = testRunDao.fetchOneByPublicId(publicId.id)

            expectThat(secondTestRun) {
                get { passed }.isFalse()
                get { totalPassingCount }.isEqualTo(3)
                get { totalFailureCount }.isEqualTo(2)
                get { totalSkippedCount }.isEqualTo(0)
                get { totalTestCount }.isEqualTo(5)
            }

            val secondTestSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(secondTestSuites).hasSize(4)

            val secondTestSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
            expectThat(secondTestSuiteGroups)
                .hasSize(2)

            val secondTestSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
            assertNotNull(secondTestSuiteGroup1)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(secondTestSuiteGroup1.id)).hasSize(3)

            val secondTestSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
            assertNotNull(secondTestSuiteGroup2)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(secondTestSuiteGroup2.id)).hasSize(1)
        }

    @Test
    fun `should append second and third grouped test results runs`() =
        projektorTestApplication {
            val repoPart = RandomStringUtils.randomAlphabetic(12)

            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "craigatk/$repoPart"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val metadata = ResultsMetadata()
            metadata.git = gitMetadata
            metadata.group = "B12"

            val initialRequestBody = GroupedResultsXmlLoader().passingGroupedResults(metadata)

            lateinit var publicId: PublicId
            lateinit var testRun: TestRun

            val initialResponse = client.postGroupedResultsJSON(initialRequestBody)

            val initialSaveComplete = waitForTestRunSaveToComplete(initialResponse)
            publicId = initialSaveComplete.first
            testRun = initialSaveComplete.second

            expectThat(testRun) {
                get { passed }.isTrue()
                get { totalPassingCount }.isEqualTo(3)
                get { totalFailureCount }.isEqualTo(0)
                get { totalSkippedCount }.isEqualTo(0)
                get { totalTestCount }.isEqualTo(3)
            }

            val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuites).hasSize(3)

            val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
            expectThat(testSuiteGroups).hasSize(2)

            val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
            assertNotNull(testSuiteGroup1)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(2)

            val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
            assertNotNull(testSuiteGroup2)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)

            await until { resultsProcessingDao.fetchOneByPublicId(publicId.id).status == ResultsProcessingStatus.SUCCESS.name }

            val secondRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), metadata)
            val secondResponse = client.postGroupedResultsJSON(secondRequestBody)

            val responseContent = secondResponse.bodyAsText()
            assertNotNull(responseContent)
            val resultsResponse: SaveResultsResponse = objectMapper.readValue(responseContent)

            val secondPublicIdResponse = resultsResponse.id
            expectThat(secondPublicIdResponse).isEqualTo(publicId.id)

            val (secondPublicId, _) = waitForTestRunSaveToComplete(secondResponse)
            expectThat(secondPublicId).isEqualTo(publicId)

            await until {
                val updatedTestRun = testRunDao.fetchOneByPublicId(publicId.id)
                !updatedTestRun.passed
            }

            val secondTestRun = testRunDao.fetchOneByPublicId(publicId.id)

            expectThat(secondTestRun) {
                get { passed }.isFalse()
                get { totalPassingCount }.isEqualTo(3)
                get { totalFailureCount }.isEqualTo(2)
                get { totalSkippedCount }.isEqualTo(0)
                get { totalTestCount }.isEqualTo(5)
            }

            val testSuitesSecond = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuitesSecond).hasSize(4)

            val testSuiteGroupsSecond = testSuiteGroupDao.fetchByTestRunId(testRun.id)
            expectThat(testSuiteGroupsSecond).hasSize(2)

            val testSuiteGroup1Second = testSuiteGroups.find { it.groupName == "Group1" }
            assertNotNull(testSuiteGroup1Second)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1Second.id)).hasSize(3)

            val testSuiteGroup2Second = testSuiteGroups.find { it.groupName == "Group2" }
            assertNotNull(testSuiteGroup2Second)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2Second.id)).hasSize(1)

            val thirdRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.someIgnored(), metadata)
            val thirdResponse = client.postGroupedResultsJSON(thirdRequestBody)

            val responseContentThird = thirdResponse.bodyAsText()
            assertNotNull(responseContentThird)
            val resultsResponseThird: SaveResultsResponse = objectMapper.readValue(responseContentThird)

            val thirdPublicIdResponse = resultsResponseThird.id
            expectThat(thirdPublicIdResponse).isEqualTo(publicId.id)

            val (thirdPublicId, _) = waitForTestRunSaveToComplete(thirdResponse)
            expectThat(thirdPublicId).isEqualTo(publicId)

            await until {
                val updatedTestRun = testRunDao.fetchOneByPublicId(publicId.id)
                updatedTestRun.totalSkippedCount > 0
            }

            val secondTestRunThird = testRunDao.fetchOneByPublicId(publicId.id)

            expectThat(secondTestRunThird) {
                get { passed }.isFalse()
                get { totalPassingCount }.isEqualTo(10)
                get { totalFailureCount }.isEqualTo(2)
                get { totalSkippedCount }.isEqualTo(3)
                get { totalTestCount }.isEqualTo(15)
            }

            val testSuitesThird = testSuiteDao.fetchByTestRunId(testRun.id)
            expectThat(testSuitesThird).hasSize(5)

            val testSuiteGroupsThird = testSuiteGroupDao.fetchByTestRunId(testRun.id)
            expectThat(testSuiteGroupsThird).hasSize(2)

            val testSuiteGroup1Third = testSuiteGroups.find { it.groupName == "Group1" }
            assertNotNull(testSuiteGroup1Third)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1Third.id)).hasSize(4)

            val testSuiteGroup2Third = testSuiteGroups.find { it.groupName == "Group2" }
            assertNotNull(testSuiteGroup2Third)
            expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2Third.id)).hasSize(1)
        }
}
