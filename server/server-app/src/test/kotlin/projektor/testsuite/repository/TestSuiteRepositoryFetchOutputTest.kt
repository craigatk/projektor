package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestRunGenerator
import projektor.server.api.PublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

abstract class TestSuiteRepositoryFetchOutputTest : RepositoryTestCase() {
    private fun createTestRunWithOutput(publicId: PublicId) {
        val testSuite =
            TestRunGenerator.toParsedTestSuite(TestSuiteData("TestSuite", listOf("testCase1"), listOf(), listOf())).apply {
                systemOut = "My system out"
                systemErr = "My system err"
            }

        runBlocking { testRunRepository.saveTestRun(publicId, listOf(testSuite)) }
    }

    @Test
    fun `when test suite has no output should return no output`() {
        val publicId = randomPublicId()
        testRunGenerator.createSimpleTestRun(publicId)

        expectThat(runBlocking { testSuiteRepository.fetchTestSuiteSystemOut(publicId, 1) }).get { value }.isNull()
        expectThat(runBlocking { testSuiteRepository.fetchTestSuiteSystemErr(publicId, 1) }).get { value }.isNull()
    }

    @Test
    fun `should fetch test suite system out`() {
        val publicId = randomPublicId()

        createTestRunWithOutput(publicId)

        val systemOut =
            runBlocking {
                testSuiteRepository.fetchTestSuiteSystemOut(publicId, 1)
            }

        expectThat(systemOut)
            .get { value }.isNotNull().isEqualTo("My system out")
    }

    @Test
    fun `should fetch test suite system err`() {
        val publicId = randomPublicId()

        createTestRunWithOutput(publicId)

        val systemErr =
            runBlocking {
                testSuiteRepository.fetchTestSuiteSystemErr(publicId, 1)
            }

        expectThat(systemErr)
            .get { value }.isNotNull().isEqualTo("My system err")
    }
}

class TestSuiteRepositoryFetchOutputDatabaseTest : TestSuiteRepositoryFetchOutputTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestSuiteRepositoryFetchOutputObjectStoreTest : TestSuiteRepositoryFetchOutputTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
