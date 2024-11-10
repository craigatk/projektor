package projektor.incomingresults

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunService
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.test.assertNotNull

class CypressResultsWithFileNameApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should save Cypress file path as package name`() =
        testSuspend {
            val resultsBody =
                GroupedResultsXmlLoader().wrapResultsXmlsInGroup(
                    listOf(
                        ResultsXmlLoader().cypressAttachmentsSpecWithFilePath(),
                        ResultsXmlLoader().cypressRepositoryTimelineSpecWithFilePath(),
                    ),
                )

            val response = postGroupedResultsJSON(resultsBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            val testRunService: TestRunService = getApplication().get()

            val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
            assertNotNull(testRun)
            expectThat(testRun) {
                get { summary }.get { totalTestCount }.isEqualTo(4)
            }

            val testSuites = testRun.testSuites
            assertNotNull(testSuites)
            expectThat(testSuites).hasSize(2).and {
                any {
                    get { fileName }.isEqualTo("cypress/integration/attachments.spec.js")
                    get { className }.isEqualTo("test run with attachments")
                    get { packageName }.isNull()
                }
                any {
                    get { fileName }.isEqualTo("cypress/integration/repository_timeline.spec.js")
                    get { className }.isEqualTo("repository coverage")
                    get { packageName }.isNull()
                }
            }

            val attachmentsTestSuite = testSuites.find { it.className == "test run with attachments" }
            assertNotNull(attachmentsTestSuite)

            val testCaseService: TestCaseService = getApplication().get()

            val testCase1 = runBlocking { testCaseService.fetchTestCase(publicId, attachmentsTestSuite.idx, 1) }
            expectThat(testCase1).isNotNull().and {
                get { name }.isEqualTo("test run with attachments should list attachments on attachments page")
                get { testSuiteName }.isEqualTo("test run with attachments")
            }
        }
}
