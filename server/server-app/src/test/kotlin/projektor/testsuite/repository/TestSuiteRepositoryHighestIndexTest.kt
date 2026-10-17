package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

abstract class TestSuiteRepositoryHighestIndexTest : RepositoryTestCase() {
    @Test
    fun `should fetch highest test suite index`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf(),
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                    listOf(),
                    listOf(),
                ),
            ),
        )

        val highestTestSuiteIdx = runBlocking { testSuiteRepository.fetchHighestTestSuiteIndex(publicId) }

        expectThat(highestTestSuiteIdx).isNotNull().isEqualTo(2)
    }
}

class TestSuiteRepositoryHighestIndexDatabaseTest : TestSuiteRepositoryHighestIndexTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestSuiteRepositoryHighestIndexObjectStoreTest : TestSuiteRepositoryHighestIndexTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
