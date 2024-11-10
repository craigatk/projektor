package projektor.incomingresults

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.testrun.TestRunService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class JestResultsWithFilePathApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should parse readable package and class names from Jest results when they include the file path`() =
        testSuspend {
            val resultsBody = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().jestUiFilePath())

            val response = postGroupedResultsJSON(resultsBody)

            val (publicId, _) = waitForTestRunSaveToComplete(response)

            val testRunService: TestRunService = getApplication().get()

            val testRun = runBlocking { testRunService.fetchTestRun(publicId) }
            expectThat(testRun).isNotNull().and {
                get { summary }.get { totalTestCount }.isEqualTo(120)
            }

            val testSuiteRepositoryCoveragePage = testRun?.testSuites?.find { it.className == "RepositoryCoveragePage" }
            expectThat(testSuiteRepositoryCoveragePage).isNotNull().and {
                get { packageName }.isEqualTo("src/Repository/Coverage/__tests__/RepositoryCoveragePage.spec.tsx")
            }
        }
}
