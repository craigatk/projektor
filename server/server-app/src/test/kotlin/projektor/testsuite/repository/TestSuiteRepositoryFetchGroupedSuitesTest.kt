package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import projektor.persistence.TestSuiteGroupData
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

abstract class TestSuiteRepositoryFetchGroupedSuitesTest : RepositoryTestCase() {
    @Test
    fun `should fetch grouped test suites`() {
        val publicId = randomPublicId()

        testRunGenerator.createGroupedTestRun(
            publicId,
            listOf(
                TestSuiteGroupData(
                    "MyGroup",
                    "MyLabel",
                    listOf(
                        TestSuiteData("projektor.TestSuite1", listOf("testCase1"), listOf(), listOf()),
                        TestSuiteData("projektor.TestSuite2", listOf("testCase2"), listOf(), listOf()),
                    ),
                ),
            ),
        )

        val testSuite1 = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 1) }
        expectThat(testSuite1)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("MyGroup")
                get { groupLabel }.isEqualTo("MyLabel")
            }

        val testSuite2 = runBlocking { testSuiteRepository.fetchTestSuite(publicId, 2) }
        expectThat(testSuite2)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("MyGroup")
                get { groupLabel }.isEqualTo("MyLabel")
            }
    }
}

class TestSuiteRepositoryFetchGroupedSuitesDatabaseTest : TestSuiteRepositoryFetchGroupedSuitesTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestSuiteRepositoryFetchGroupedSuitesObjectStoreTest : TestSuiteRepositoryFetchGroupedSuitesTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
