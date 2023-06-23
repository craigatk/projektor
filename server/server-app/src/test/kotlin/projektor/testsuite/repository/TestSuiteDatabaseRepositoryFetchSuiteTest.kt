package projektor.testsuite.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.Tables
import projektor.database.generated.tables.pojos.TestSuite
import projektor.incomingresults.randomPublicId
import projektor.testsuite.TestSuiteDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class TestSuiteDatabaseRepositoryFetchSuiteTest : DatabaseRepositoryTestCase() {

    @Test
    fun `when test suite failed should fetch test suite along with test cases and failures`() {
        val testSuiteDatabaseRepository = TestSuiteDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf()
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2TestCase1", "testSuite2TestCase2", "testSuite2TestCase3"),
                    listOf(),
                    listOf()
                )
            )
        )

        val testSuite = runBlocking { testSuiteDatabaseRepository.fetchTestSuite(publicId, 1) }

        expectThat(testSuite)
            .isNotNull()
            .and {
                get { className }.isEqualTo("testSuite1")
                get { passingCount }.isEqualTo(1)
                get { failureCount }.isEqualTo(2)
                get { skippedCount }.isEqualTo(0)
                get { testCount }.isEqualTo(3)
            }

        val testCases = testSuite?.testCases

        expectThat(testCases)
            .isNotNull()
            .any {
                get { name }.isEqualTo("testSuite1FailedTestCase1")
                get { className }.isEqualTo("testSuite1FailedTestCase1ClassName")
                get { idx }.isEqualTo(2)
                get { testSuiteIdx }.isEqualTo(1)
            }
            .any {
                get { name }.isEqualTo("testSuite1FailedTestCase2")
                get { className }.isEqualTo("testSuite1FailedTestCase2ClassName")
                get { idx }.isEqualTo(3)
                get { testSuiteIdx }.isEqualTo(1)
            }

        val failedTestCase1 = testCases?.find { it.name == "testSuite1FailedTestCase1" }
        expectThat(failedTestCase1)
            .isNotNull()
            .get { failure }
            .isNotNull()
            .and {
                get { failureMessage }.isNotNull().isEqualTo("testSuite1FailedTestCase1 failure message")
                get { failureType }.isNotNull().isEqualTo("testSuite1FailedTestCase1 failure type")
                get { failureText }.isNotNull().isEqualTo("testSuite1FailedTestCase1 failure text")
            }

        val failedTestCase2 = testCases?.find { it.name == "testSuite1FailedTestCase2" }
        expectThat(failedTestCase2)
            .isNotNull()
            .get { failure }
            .isNotNull()
            .and {
                get { failureMessage }.isNotNull().isEqualTo("testSuite1FailedTestCase2 failure message")
                get { failureType }.isNotNull().isEqualTo("testSuite1FailedTestCase2 failure type")
                get { failureText }.isNotNull().isEqualTo("testSuite1FailedTestCase2 failure text")
            }
    }

    @Test
    fun `should not fetch system out and err columns when fetching individual test suite`() {
        val publicId = randomPublicId()

        val testRunDB = testRunDBGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1"),
                    listOf(),
                    listOf()
                )
            )
        )

        val testSuiteIdx = 1

        dslContext.update(Tables.TEST_SUITE)
            .set(Tables.TEST_SUITE.SYSTEM_OUT, "system out")
            .set(Tables.TEST_SUITE.SYSTEM_ERR, "system err")
            .where(Tables.TEST_SUITE.TEST_RUN_ID.eq(testRunDB.id).and(Tables.TEST_SUITE.IDX.eq(testSuiteIdx)))
            .execute()

        val testSuites = TestSuiteDatabaseRepository.selectTestSuite(dslContext)
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
