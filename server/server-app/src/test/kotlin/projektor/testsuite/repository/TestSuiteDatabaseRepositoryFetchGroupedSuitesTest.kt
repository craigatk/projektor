package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB

class TestSuiteDatabaseRepositoryFetchGroupedSuitesTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch grouped test suites`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "projektor.TestSuite1",
                    listOf("testCase1"),
                    listOf(),
                    listOf()
                ),
                TestSuiteData(
                    "projektor.TestSuite2",
                    listOf("testCase2"),
                    listOf(),
                    listOf()
                )
            )
        )

        val testSuiteGroup = TestSuiteGroupDB()
        testSuiteGroup.testRunId = testRun.id
        testSuiteGroup.groupName = "MyGroup"
        testSuiteGroup.groupLabel = "MyLabel"
        testSuiteGroupDao.insert(testSuiteGroup)

        testRunDBGenerator.addTestSuiteGroupToTestRun(testSuiteGroup, testRun, listOf("TestSuite1", "TestSuite2"))

        val testSuite1 = runBlocking { testSuiteDatabaseRepository.fetchTestSuite(publicId, 1) }
        expectThat(testSuite1)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("MyGroup")
                get { groupLabel }.isEqualTo("MyLabel")
            }

        val testSuite2 = runBlocking { testSuiteDatabaseRepository.fetchTestSuite(publicId, 2) }
        expectThat(testSuite2)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("MyGroup")
                get { groupLabel }.isEqualTo("MyLabel")
            }
    }
}
