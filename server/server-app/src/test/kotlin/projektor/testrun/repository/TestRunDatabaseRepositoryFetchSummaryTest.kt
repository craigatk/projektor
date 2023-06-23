package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.pojos.TestRun
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import java.time.LocalDateTime

class TestRunDatabaseRepositoryFetchSummaryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch all fields in the test run summary`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)
        val publicId = randomPublicId()

        val testRunDB = TestRun()
        testRunDB.publicId = publicId.id
        testRunDB.totalTestCount = 3
        testRunDB.totalPassingCount = 1
        testRunDB.totalFailureCount = 1
        testRunDB.totalSkippedCount = 1
        testRunDB.passed = false
        testRunDB.cumulativeDuration = BigDecimal("9.00")
        testRunDB.averageDuration = BigDecimal("3.00")
        testRunDB.slowestTestCaseDuration = BigDecimal("5.00")
        testRunDB.createdTimestamp = LocalDateTime.now()
        testRunDao.insert(testRunDB)

        val testRunSummary = runBlocking { testRunDatabaseRepository.fetchTestRunSummary(publicId) }

        expectThat(testRunSummary)
            .isNotNull()
            .and {
                get { totalTestCount }.isEqualTo(3)
                get { totalPassingCount }.isEqualTo(1)
                get { totalFailureCount }.isEqualTo(1)
                get { totalSkippedCount }.isEqualTo(1)
                get { cumulativeDuration }.isEqualTo(BigDecimal("9.000"))
                get { averageDuration }.isEqualTo(BigDecimal("3.000"))
                get { slowestTestCaseDuration }.isEqualTo(BigDecimal("5.000"))
            }
    }
}
