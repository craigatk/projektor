package projektor.testsuite.repository

import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.Tables
import projektor.database.generated.tables.pojos.TestSuite
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNull

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
}
