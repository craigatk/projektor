package projektor.incomingresults

import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import strikt.api.expectThat
import strikt.assertions.isNotEmpty
import kotlin.test.assertNotNull

class GroupedTestResultsServiceTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should filter out test suites that have no test cases`() {
        val groupedTestResultsService: GroupedTestResultsService by inject()
        val groupedResultsConverter: GroupedResultsConverter by inject()

        val publicId = randomPublicId()

        val resultXml = ResultsXmlLoader().cypressResults().joinToString("\n")
        val groupedResultsJson = GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultXml)
        val groupedResults = runBlocking { groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsJson) }

        runBlocking { groupedTestResultsService.doPersistTestResults(publicId, groupedResults, groupedResultsJson) }

        val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId.id) }
        assertNotNull(testRun)

        val testSuites = testSuiteDao.fetchByTestRunId(testRun.id)

        testSuites.forEach { testSuite ->
            val testCases = testCaseDao.fetchByTestSuiteId(testSuite.id)
            expectThat(testCases).isNotEmpty()
        }
    }
}
