package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.Tables
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.containsExactly

/**
 * Checks the database repository's query, which only applies to the database backend.
 */
class TestRunDatabaseRepositoryQueryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should fetch test suites in order when their rows are stored out of order`() {
        val publicId = randomPublicId()

        val testRunDB =
            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData("testSuite1", listOf("testCase1"), listOf(), listOf()),
                    TestSuiteData("testSuite2", listOf("testCase1"), listOf(), listOf()),
                    TestSuiteData("testSuite3", listOf("testCase1"), listOf(), listOf()),
                ),
            )

        // Updating an indexed column writes the row to a new location, so without an order it comes back last
        listOf(999, 1).forEach { newIdx ->
            dslContext.update(Tables.TEST_SUITE)
                .set(Tables.TEST_SUITE.IDX, newIdx)
                .where(Tables.TEST_SUITE.TEST_RUN_ID.eq(testRunDB.id).and(Tables.TEST_SUITE.CLASS_NAME.eq("testSuite1")))
                .execute()
        }

        val testRun = runBlocking { TestRunDatabaseRepository(dslContext).fetchTestRun(publicId) }

        expectThat(testRun?.testSuites.orEmpty().map { it.idx }).containsExactly(1, 2, 3)
    }
}
