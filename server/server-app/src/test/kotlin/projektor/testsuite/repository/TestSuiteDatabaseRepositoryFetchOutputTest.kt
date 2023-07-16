package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.createTestRun
import projektor.database.generated.tables.pojos.TestSuite
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestSuiteDatabaseRepositoryFetchOutputTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch test suite system out`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRun = createTestRun(publicId, 1)
        testRunDao.insert(testRun)

        val testSuiteDB = TestSuite()
        testSuiteDB.testRunId = testRun.id
        testSuiteDB.idx = 1
        testSuiteDB.testCount = 1
        testSuiteDB.passingCount = 1
        testSuiteDB.failureCount = 0
        testSuiteDB.skippedCount = 0
        testSuiteDB.className = "TestSuite"
        testSuiteDB.hasSystemOut = true
        testSuiteDB.hasSystemErr = true
        testSuiteDB.systemOut = "My system out"
        testSuiteDB.systemErr = "My system err"
        testSuiteDao.insert(testSuiteDB)

        val systemOut = runBlocking {
            testSuiteDatabaseRepository.fetchTestSuiteSystemOut(publicId, 1)
        }

        expectThat(systemOut)
            .get { value }.isNotNull().isEqualTo("My system out")
    }

    @Test
    fun `should fetch test suite system err`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRun = createTestRun(publicId, 1)
        testRunDao.insert(testRun)

        val testSuiteDB = TestSuite()
        testSuiteDB.testRunId = testRun.id
        testSuiteDB.idx = 1
        testSuiteDB.testCount = 1
        testSuiteDB.passingCount = 1
        testSuiteDB.failureCount = 0
        testSuiteDB.skippedCount = 0
        testSuiteDB.className = "TestSuite"
        testSuiteDB.hasSystemOut = true
        testSuiteDB.hasSystemErr = true
        testSuiteDB.systemOut = "My system out"
        testSuiteDB.systemErr = "My system err"
        testSuiteDao.insert(testSuiteDB)

        val systemErr = runBlocking {
            testSuiteDatabaseRepository.fetchTestSuiteSystemErr(publicId, 1)
        }

        expectThat(systemErr)
            .get { value }.isNotNull().isEqualTo("My system err")
    }
}
