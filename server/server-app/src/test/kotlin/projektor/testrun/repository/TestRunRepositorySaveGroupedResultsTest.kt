package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import projektor.parser.model.TestCase as ParsedTestCase
import projektor.parser.model.TestSuite as ParsedTestSuite

abstract class TestRunRepositorySaveGroupedResultsTest : RepositoryTestCase() {
    @Test
    fun `should save grouped results with two groups with two test suites each`() {
        val publicId = randomPublicId()

        val testGroup1TestSuite1 = createTestSuite("testGroup1TestSuite1")
        val testGroup1TestSuite2 = createTestSuite("testGroup1TestSuite2")

        val testGroup1 =
            GroupedTestSuites(
                listOf(testGroup1TestSuite1, testGroup1TestSuite2),
                "Group1",
                "Label1",
                "directory-1",
            )

        val testGroup2TestSuite1 = createTestSuite("testGroup2TestSuite1")
        val testGroup2TestSuite2 = createTestSuite("testGroup2TestSuite2")

        val testGroup2 =
            GroupedTestSuites(
                listOf(testGroup2TestSuite1, testGroup2TestSuite2),
                "Group2",
                "Label2",
                "directory-2",
            )

        val groupedResults = GroupedResults(listOf(testGroup1, testGroup2), listOf(), null, null, null, null)

        runBlocking { testRunRepository.saveGroupedTestRun(publicId, groupedResults) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNotNull()

        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites).hasSize(4)

        val group1TestSuites = testSuites.filter { it.groupName == "Group1" }
        expectThat(group1TestSuites)
            .hasSize(2)
            .all { get { groupLabel }.isEqualTo("Label1") }
        expectThat(group1TestSuites.map { it.className }).containsExactly("testGroup1TestSuite1", "testGroup1TestSuite2")

        val group2TestSuites = testSuites.filter { it.groupName == "Group2" }
        expectThat(group2TestSuites)
            .hasSize(2)
            .all { get { groupLabel }.isEqualTo("Label2") }
        expectThat(group2TestSuites.map { it.className }).containsExactly("testGroup2TestSuite1", "testGroup2TestSuite2")
    }

    @Test
    fun `can save grouped test run with no test suites`() {
        val publicId = randomPublicId()

        val testGroup =
            GroupedTestSuites(
                listOf(),
                "Group",
                "Label",
                "directory",
            )

        val groupedResults = GroupedResults(listOf(testGroup), listOf(), null, null, null, null)

        runBlocking { testRunRepository.saveGroupedTestRun(publicId, groupedResults) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNotNull()
        expectThat(runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }).hasSize(0)
    }

    private fun createTestSuite(name: String): ParsedTestSuite {
        val testSuite = ParsedTestSuite()
        testSuite.name = name
        testSuite.tests = 2
        testSuite.skipped = 0
        testSuite.failures = 0
        testSuite.errors = 0
        // The database repository can't read back a test suite without a duration, see OBJECT_STORE_PLAN.md
        testSuite.time = BigDecimal("1.5")

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

class TestRunRepositorySaveGroupedResultsDatabaseTest : TestRunRepositorySaveGroupedResultsTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositorySaveGroupedResultsObjectStoreTest : TestRunRepositorySaveGroupedResultsTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
