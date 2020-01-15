package projektor.incomingresults

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.koin.core.inject
import projektor.DatabaseRepositoryTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.isNotEmpty

class GroupedTestResultsServiceTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should filter out test suites that have no test cases`() {
        val groupedTestResultsService: GroupedTestResultsService by inject()

        val publicId = randomPublicId()

        val resultXml = ResultsXmlLoader().cypressResults().joinToString("\n")
        val groupedResultsJson = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultXml)

        runBlocking { groupedTestResultsService.doPersistTestResults(publicId, groupedResultsJson) }

        val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId.id) }
        assertNotNull(testRun)

        val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

        testSuites.forEach { testSuite ->
            val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
            expectThat(testCases).isNotEmpty()
        }
    }
}
