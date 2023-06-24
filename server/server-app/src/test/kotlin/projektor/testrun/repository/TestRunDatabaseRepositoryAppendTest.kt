package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.GroupedResultsConverter
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.testrun.TestRunDatabaseRepository
import projektor.testsuite.TestSuiteRepository
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.map

class TestRunDatabaseRepositoryAppendTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should append new test group of test suites`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val groupedResultsConverter: GroupedResultsConverter by inject()
        val testSuiteRepository: TestSuiteRepository by inject()

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                    listOf("testSuite2FailedTestCase1"),
                    listOf()
                )
            )
        )

        testRunDBGenerator.addTestSuiteGroupToTestRun("SomeGroup1", testRun, listOf("testSuite1"))
        testRunDBGenerator.addTestSuiteGroupToTestRun("SomeGroup2", testRun, listOf("testSuite2"))

        val groupedResultsBlob = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().failing())
        val groupedResults = runBlocking { groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob) }

        runBlocking { testRunDatabaseRepository.appendTestSuites(publicId, 2, groupedResults.groupedTestSuites) }

        val addedTestSuite = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 3) }
        expectThat(addedTestSuite).isNotNull().and {
            get { className }.isEqualTo("FailingSpec")
        }

        expectThat(testSuiteGroupDao.fetchByTestRunId(testRun.id))
            .hasSize(3)
            .map { it.groupName }.contains("Group1", "SomeGroup1", "SomeGroup2")
    }

    @Test
    fun `should append test suites to existing group`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val groupedResultsConverter: GroupedResultsConverter by inject()
        val testSuiteRepository: TestSuiteRepository by inject()

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                    listOf("testSuite2FailedTestCase1"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite3",
                    listOf("testSuite3PassedTestCase1", "testSuite3PassedTestCase2"),
                    listOf("testSuite3FailedTestCase1"),
                    listOf()
                )
            )
        )

        testRunDBGenerator.addTestSuiteGroupToTestRun("Group1", testRun, listOf("testSuite1"))
        testRunDBGenerator.addTestSuiteGroupToTestRun("Group2", testRun, listOf("testSuite2", "testSuite3"))

        val groupedResultsBlob = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().failing())
        val groupedResults = runBlocking { groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob) }

        runBlocking { testRunDatabaseRepository.appendTestSuites(publicId, 3, groupedResults.groupedTestSuites) }

        val addedTestSuite = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 4) }
        expectThat(addedTestSuite).isNotNull().and {
            get { className }.isEqualTo("FailingSpec")
        }

        expectThat(testSuiteGroupDao.fetchByTestRunId(testRun.id))
            .hasSize(2)
            .map { it.groupName }.contains("Group1", "Group2")
    }
}
