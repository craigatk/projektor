package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.isNull

class TestRunDatabaseRepositoryDeleteTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should delete test run`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(publicId,
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

        val testSuiteIds = testSuiteDao.fetchByTestRunId(testRun.id).map { it.id }
        val testCaseIds = testSuiteIds.flatMap { testCaseDao.fetchByTestSuiteId(it) }.map { it.id }

        runBlocking { testRunDatabaseRepository.deleteTestRun(publicId) }

        expectThat(testRunDao.fetchOneById(testRun.id)).isNull()

        testSuiteIds.forEach {
            expectThat(testSuiteDao.fetchOneById(it)).isNull()
        }

        testCaseIds.forEach {
            expectThat(testCaseDao.fetchOneById(it)).isNull()
        }
    }
}
