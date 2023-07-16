package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

class TestRunDatabaseRepositorySaveGroupedResultsTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should save grouped results with two groups with two test suites each`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val testGroup1TestSuite1 = createTestSuite("testGroup1TestSuite1")
        val testGroup1TestSuite2 = createTestSuite("testGroup1TestSuite2")

        val testGroup1 = GroupedTestSuites(
            listOf(testGroup1TestSuite1, testGroup1TestSuite2),
            "Group1",
            "Label1",
            "directory-1"
        )

        val testGroup2TestSuite1 = createTestSuite("testGroup2TestSuite1")
        val testGroup2TestSuite2 = createTestSuite("testGroup2TestSuite2")

        val testGroup2 = GroupedTestSuites(
            listOf(testGroup2TestSuite1, testGroup2TestSuite2),
            "Group2",
            "Label2",
            "directory-2"
        )

        val groupedResults = GroupedResults(listOf(testGroup1, testGroup2), listOf(), null, null, null, null)

        runBlocking { testRunDatabaseRepository.saveGroupedTestRun(publicId, groupedResults) }

        val testRunDB = testRunDao.fetchOneByPublicId(publicId.id)
        assertNotNull(testRunDB)

        val testGroupsDB = testSuiteGroupDao.fetchByTestRunId(testRunDB.id)
        expectThat(testGroupsDB)
            .hasSize(2)

        val testGroup1DB = testGroupsDB.find { it.groupName == "Group1" }
        expectThat(testGroup1DB)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("Group1")
                get { groupLabel }.isEqualTo("Label1")
                get { directory }.isEqualTo("directory-1")
            }

        val testGroup1SuitesDB = testSuiteDao.fetchByTestSuiteGroupId(testGroup1DB!!.id)
        expectThat(testGroup1SuitesDB)
            .hasSize(2)

        val testGroup2DB = testGroupsDB.find { it.groupName == "Group2" }
        expectThat(testGroup2DB)
            .isNotNull()
            .and {
                get { groupName }.isEqualTo("Group2")
                get { groupLabel }.isEqualTo("Label2")
                get { directory }.isEqualTo("directory-2")
            }

        val testGroup2SuitesDB = testSuiteDao.fetchByTestSuiteGroupId(testGroup2DB!!.id)
        expectThat(testGroup2SuitesDB)
            .hasSize(2)
    }

    @Test
    fun `can save grouped test run with no test suites`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testGroup = GroupedTestSuites(
            listOf(),
            "Group",
            "Label",
            "directory"
        )

        val groupedResults = GroupedResults(listOf(testGroup), listOf(), null, null, null, null)

        runBlocking { testRunDatabaseRepository.saveGroupedTestRun(publicId, groupedResults) }

        val testRunDB = testRunDao.fetchOneByPublicId(publicId.id)
        assertNotNull(testRunDB)

        val testSuitesDB = testSuiteDao.fetchByTestRunId(testRunDB.id)
        expectThat(testSuitesDB)
            .hasSize(0)
    }

    private fun createTestSuite(name: String): ParsedTestSuite {
        val testSuite = ParsedTestSuite()
        testSuite.name = name
        testSuite.tests = 2
        testSuite.skipped = 0
        testSuite.failures = 0
        testSuite.errors = 0

        val testCase1 = ParsedTestCase()
        testCase1.name = "${name}TestCase1"
        testCase1.className = "${name}TestCase1Class"

        val testCase2 = ParsedTestCase()
        testCase2.name = "${name}TestCase2"
        testCase2.className = "${name}TestCase2Class"

        testSuite.testCases = listOf(testCase1, testCase2)

        return testSuite
    }
}
