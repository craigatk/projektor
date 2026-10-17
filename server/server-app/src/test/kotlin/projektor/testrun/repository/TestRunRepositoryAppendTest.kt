package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.GroupedResultsConverter
import projektor.incomingresults.randomPublicId
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.performance.PerformanceResultsParser
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestSuiteGroupData
import projektor.results.processor.TestResultsProcessor
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.map

abstract class TestRunRepositoryAppendTest : RepositoryTestCase() {
    private val groupedResultsConverter =
        GroupedResultsConverter(
            GroupedResultsParser(GroupedResultsParser.DEFAULT_MAX_PAYLOAD_SIZE),
            PerformanceResultsParser(),
            TestResultsProcessor(),
        )

    @Test
    fun `should append new test group of test suites`() {
        val publicId = randomPublicId()

        testRunGenerator.createGroupedTestRun(
            publicId,
            listOf(
                TestSuiteGroupData(
                    "SomeGroup1",
                    null,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                            listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                            listOf(),
                        ),
                    ),
                ),
                TestSuiteGroupData(
                    "SomeGroup2",
                    null,
                    listOf(
                        TestSuiteData(
                            "testSuite2",
                            listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                            listOf("testSuite2FailedTestCase1"),
                            listOf(),
                        ),
                    ),
                ),
            ),
        )

        val groupedResultsBlob = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().failing())
        val groupedResults = runBlocking { groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob) }

        runBlocking { testRunRepository.appendTestSuites(publicId, 2, groupedResults.groupedTestSuites) }

        val addedTestSuite = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 3) }
        expectThat(addedTestSuite).isNotNull().and {
            get { className }.isEqualTo("FailingSpec")
            get { groupName }.isEqualTo("Group1")
        }

        val groupNames = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }.map { it.groupName }.distinct()
        expectThat(groupNames).hasSize(3).contains("Group1", "SomeGroup1", "SomeGroup2")
    }

    @Test
    fun `should append test suites to existing group`() {
        val publicId = randomPublicId()

        testRunGenerator.createGroupedTestRun(
            publicId,
            listOf(
                TestSuiteGroupData(
                    "Group1",
                    "Label1",
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                            listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                            listOf(),
                        ),
                    ),
                ),
                TestSuiteGroupData(
                    "Group2",
                    null,
                    listOf(
                        TestSuiteData(
                            "testSuite2",
                            listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                            listOf("testSuite2FailedTestCase1"),
                            listOf(),
                        ),
                        TestSuiteData(
                            "testSuite3",
                            listOf("testSuite3PassedTestCase1", "testSuite3PassedTestCase2"),
                            listOf("testSuite3FailedTestCase1"),
                            listOf(),
                        ),
                    ),
                ),
            ),
        )

        val groupedResultsBlob = GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().failing())
        val groupedResults = runBlocking { groupedResultsConverter.parseAndConvertGroupedResults(groupedResultsBlob) }

        runBlocking { testRunRepository.appendTestSuites(publicId, 3, groupedResults.groupedTestSuites) }

        val addedTestSuite = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 4) }
        expectThat(addedTestSuite).isNotNull().and {
            get { className }.isEqualTo("FailingSpec")
            // Appended to the existing group, so it keeps that group's label
            get { groupName }.isEqualTo("Group1")
            get { groupLabel }.isEqualTo("Label1")
        }

        val groupNames = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }.map { it.groupName }.distinct()
        expectThat(groupNames).hasSize(2).contains("Group1", "Group2")
    }
}

class TestRunRepositoryAppendDatabaseTest : TestRunRepositoryAppendTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositoryAppendObjectStoreTest : TestRunRepositoryAppendTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
