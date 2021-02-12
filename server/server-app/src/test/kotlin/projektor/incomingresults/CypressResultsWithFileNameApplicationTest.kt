package projektor.incomingresults

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.testrun.TestRunService
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class CypressResultsWithFileNameApplicationTest : ApplicationTestCase() {
    @Test
    fun `should save Cypress file path as package name`() {
        val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(
            ResultsXmlLoader().cypressAttachmentsSpecWithFilePath() +
                ResultsXmlLoader().cypressRepositoryTimelineSpecWithFilePath()
        )

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/groupedResults") {
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(resultsBody)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val (publicId, _) = waitForTestRunSaveToComplete(response)

                val testRunService: TestRunService = application.get()

                val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
                assertNotNull(testRun)
                expectThat(testRun) {
                    get { summary }.get { totalTestCount }.isEqualTo(4)
                }

                val testSuites = testRun.testSuites
                assertNotNull(testSuites)
                expectThat(testSuites).hasSize(2).and {
                    any {
                        get { packageName }.isEqualTo("cypress/integration/attachments.spec.js")
                        get { className }.isEqualTo("attachments")
                    }
                    any {
                        get { packageName }.isEqualTo("cypress/integration/repository_timeline.spec.js")
                        get { className }.isEqualTo("repository_timeline")
                    }
                }
            }
        }
    }
}
