package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.Tables
import projektor.database.generated.tables.pojos.TestSuite
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import projektor.testsuite.TestSuiteSearchCriteria
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isNull
import java.math.BigDecimal

/**
 * Checks the database repository's query, which only applies to the database backend.
 */
class TestSuiteDatabaseRepositoryQueryTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should not fetch system out and err columns when fetching individual test suite`() {
        val publicId = randomPublicId()

        val testRunDB =
            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testSuite1PassedTestCase1"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

        val testSuiteIdx = 1

        dslContext.update(Tables.TEST_SUITE)
            .set(Tables.TEST_SUITE.SYSTEM_OUT, "system out")
            .set(Tables.TEST_SUITE.SYSTEM_ERR, "system err")
            .where(Tables.TEST_SUITE.TEST_RUN_ID.eq(testRunDB.id).and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx)))
            .execute()

        val testSuites =
            TestSuiteDatabaseRepository.selectTestSuite(dslContext)
                .where(Tables.TEST_RUN.PUBLIC_ID.eq(publicId.id).and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx)))
                .orderBy(Tables.TEST_SUITE.ID)
                .fetchInto(TestSuite::class.java)

        expectThat(testSuites).hasSize(1)

        val testSuite = testSuites[0]

        expectThat(testSuite).and {
            get { systemOut }.isNull()
            get { systemErr }.isNull()
        }
    }

    @Test
    fun `should fetch test cases in order when their rows are stored out of order`() {
        val publicId = randomPublicId()
        val testRepository = TestSuiteDatabaseRepository(dslContext)

        val testRunDB =
            testRunDBGenerator.createTestRun(
                publicId,
                listOf(
                    TestSuiteData(
                        "testSuite1",
                        listOf("testCase1", "testCase2", "testCase3"),
                        listOf(),
                        listOf(),
                    ),
                ),
            )

        // Updating an indexed column writes the row to a new location, so without an order it comes back last
        val testSuiteIds =
            dslContext
                .select(Tables.TEST_SUITE.ID)
                .from(Tables.TEST_SUITE)
                .where(Tables.TEST_SUITE.TEST_RUN_ID.eq(testRunDB.id))

        dslContext.update(Tables.TEST_CASE)
            .set(Tables.TEST_CASE.DURATION, BigDecimal("9.999"))
            .where(Tables.TEST_CASE.IDX.eq(1).and(Tables.TEST_CASE.TEST_SUITE_ID.`in`(testSuiteIds)))
            .execute()

        val fetchedTestSuites =
            runBlocking {
                listOf(
                    testRepository.fetchTestSuitesWithCases(publicId).single(),
                    testRepository.fetchTestSuites(publicId, TestSuiteSearchCriteria(null, false)).single(),
                    testRepository.fetchTestSuite(publicId, 1)!!,
                )
            }

        fetchedTestSuites.forEach { testSuite ->
            expectThat(testSuite.testCases.orEmpty().map { it.idx }).containsExactly(1, 2, 3)
        }
    }
}
