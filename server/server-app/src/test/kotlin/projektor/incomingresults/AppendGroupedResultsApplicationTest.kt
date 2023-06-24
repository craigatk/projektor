package projektor.incomingresults

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
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
    fun `should append second grouped test results run`() {
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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(initialRequestBody)
            }.apply {
                val initialSaveComplete = waitForTestRunSaveToComplete(response)
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
            }
        }

        val secondRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(secondRequestBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val resultsResponse: SaveResultsResponse = objectMapper.readValue(responseContent)

                val secondPublicIdResponse = resultsResponse.id
                expectThat(secondPublicIdResponse).isEqualTo(publicId.id)

                val (secondPublicId, _) = waitForTestRunSaveToComplete(response)
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

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(4)

                val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
                expectThat(testSuiteGroups)
                    .hasSize(2)

                val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
                assertNotNull(testSuiteGroup1)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(3)

                val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
                assertNotNull(testSuiteGroup2)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)
            }
        }
    }

    @Test
    fun `should append second and third grouped test results runs`() {
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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(initialRequestBody)
            }.apply {
                val initialSaveComplete = waitForTestRunSaveToComplete(response)
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
            }
        }

        val secondRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.failing(), metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(secondRequestBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val resultsResponse: SaveResultsResponse = objectMapper.readValue(responseContent)

                val secondPublicIdResponse = resultsResponse.id
                expectThat(secondPublicIdResponse).isEqualTo(publicId.id)

                val (secondPublicId, _) = waitForTestRunSaveToComplete(response)
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

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(4)

                val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
                expectThat(testSuiteGroups).hasSize(2)

                val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
                assertNotNull(testSuiteGroup1)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(3)

                val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
                assertNotNull(testSuiteGroup2)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)
            }
        }

        val thirdRequestBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXmlLoader.someIgnored(), metadata)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(thirdRequestBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseContent = response.content
                assertNotNull(responseContent)
                val resultsResponse: SaveResultsResponse = objectMapper.readValue(responseContent)

                val thirdPublicIdResponse = resultsResponse.id
                expectThat(thirdPublicIdResponse).isEqualTo(publicId.id)

                val (thirdPublicId, _) = waitForTestRunSaveToComplete(response)
                expectThat(thirdPublicId).isEqualTo(publicId)

                await until {
                    val updatedTestRun = testRunDao.fetchOneByPublicId(publicId.id)
                    updatedTestRun.totalSkippedCount > 0
                }

                val secondTestRun = testRunDao.fetchOneByPublicId(publicId.id)

                expectThat(secondTestRun) {
                    get { passed }.isFalse()
                    get { totalPassingCount }.isEqualTo(10)
                    get { totalFailureCount }.isEqualTo(2)
                    get { totalSkippedCount }.isEqualTo(3)
                    get { totalTestCount }.isEqualTo(15)
                }

                val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)
                expectThat(testSuites).hasSize(5)

                val testSuiteGroups = testSuiteGroupDao.fetchByTestRunId(testRun.id)
                expectThat(testSuiteGroups).hasSize(2)

                val testSuiteGroup1 = testSuiteGroups.find { it.groupName == "Group1" }
                assertNotNull(testSuiteGroup1)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup1.id)).hasSize(4)

                val testSuiteGroup2 = testSuiteGroups.find { it.groupName == "Group2" }
                assertNotNull(testSuiteGroup2)
                expectThat(testSuiteDao.fetchByTestSuiteGroupId(testSuiteGroup2.id)).hasSize(1)
            }
        }
    }
}
