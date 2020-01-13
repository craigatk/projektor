package projektor.testsuite.repository

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class TestSuiteDatabaseRepositoryFetchGroupedSuitesTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch grouped test suites`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                        TestSuiteData("projektor.TestSuite1",
                                listOf("testCase1"),
                                listOf(),
                                listOf()
                        ),
                        TestSuiteData("projektor.TestSuite2",
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

        val testSuiteDBs = testSuiteDao.fetchByTestRunId(testRun.id)
        expectThat(testSuiteDBs).hasSize(2)

        val testSuite1DB = testSuiteDBs.find { it.className == "TestSuite1" }
        assertNotNull(testSuite1DB)
        testSuite1DB.testSuiteGroupId = testSuiteGroup.id
        testSuiteDao.update(testSuite1DB)

        val testSuite2DB = testSuiteDBs.find { it.className == "TestSuite2" }
        assertNotNull(testSuite2DB)
        testSuite2DB.testSuiteGroupId = testSuiteGroup.id
        testSuiteDao.update(testSuite2DB)

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
