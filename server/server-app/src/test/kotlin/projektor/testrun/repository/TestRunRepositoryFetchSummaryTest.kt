package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import java.math.BigDecimal

abstract class TestRunRepositoryFetchSummaryTest : RepositoryTestCase() {
    @Test
    fun `should fetch all fields in the test run summary`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(TestSuiteData("testSuite1", listOf("passing"), listOf("failing"), listOf("skipped"))),
        )

        val testRunSummary = runBlocking { testRunRepository.fetchTestRunSummary(publicId) }

        expectThat(testRunSummary)
            .isNotNull()
            .and {
                get { id }.isEqualTo(publicId.id)
                get { totalTestCount }.isEqualTo(3)
                get { totalPassingCount }.isEqualTo(1)
                get { totalFailureCount }.isEqualTo(1)
                get { totalSkippedCount }.isEqualTo(1)
                get { passed }.isFalse()
                get { cumulativeDuration }.isEqualTo(BigDecimal("7.500"))
                get { averageDuration }.isEqualTo(BigDecimal("2.500"))
                get { slowestTestCaseDuration }.isEqualTo(BigDecimal("2.500"))
            }
    }
}

class TestRunRepositoryFetchSummaryDatabaseTest : TestRunRepositoryFetchSummaryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositoryFetchSummaryObjectStoreTest : TestRunRepositoryFetchSummaryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
