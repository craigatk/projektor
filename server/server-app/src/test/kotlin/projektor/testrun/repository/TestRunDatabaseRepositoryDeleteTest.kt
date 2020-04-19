package projektor.testrun.repository

import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.database.generated.tables.pojos.TestRunSystemAttributes
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.isNull

class TestRunDatabaseRepositoryDeleteTest : DatabaseRepositoryTestCase() {
    @Test
    fun `should delete test run`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(publicId,
                listOf(
                        TestSuiteData(
                                "testSuite1",
                                listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                                listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                                listOf()
                        ),
                        TestSuiteData(
                                "testSuite2",
                                listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                                listOf("testSuite2FailedTestCase1"),
                                listOf()
                        )
                )
        )

        val testSuiteIds = testSuiteDao.fetchByTestRunId(testRun.id).map { it.id }
        val testCaseIds = testSuiteIds.flatMap { testCaseDao.fetchByTestSuiteId(it) }.map { it.id }

        runBlocking { testRunDatabaseRepository.deleteTestRun(publicId) }

        expectThat(testRunDao.fetchOneById(testRun.id)).isNull()

        testSuiteIds.forEach {
            expectThat(testSuiteDao.fetchOneById(it)).isNull()
        }

        testCaseIds.forEach {
            expectThat(testCaseDao.fetchOneById(it)).isNull()
        }
    }

    @Test
    fun `should find test runs created before a given date that are not pinned to delete`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val tooNewPublicId = randomPublicId()
        createTestRun(tooNewPublicId, LocalDate.of(2020, 4, 1), false)

        val pinnedPublicId = randomPublicId()
        createTestRun(pinnedPublicId, LocalDate.of(2020, 2, 1), true)

        val shouldDelete1PublicId = randomPublicId()
        createTestRun(shouldDelete1PublicId, LocalDate.of(2020, 2, 1), false)

        val shouldDelete2PublicId = randomPublicId()
        createTestRun(shouldDelete2PublicId, LocalDate.of(2020, 2, 1), false)

        val testRunsToDelete = runBlocking { testRunDatabaseRepository.findTestRunsToDelete(LocalDate.of(2020, 2, 2)) }

        expectThat(testRunsToDelete)
                .contains(shouldDelete1PublicId, shouldDelete2PublicId)
                .doesNotContain(tooNewPublicId, pinnedPublicId)
    }

    private fun createTestRun(publicId: PublicId, createdOn: LocalDate, pinned: Boolean) {
        val testRun = testRunDBGenerator.createTestRun(publicId, listOf())
        testRun.createdTimestamp = Timestamp.from(createdOn.atStartOfDay(ZoneId.of("UTC")).toInstant())
        testRunDao.update(testRun)

        val testRunSystemAttributes = TestRunSystemAttributes(publicId.id, pinned)
        testRunSystemAttributesDao.insert(testRunSystemAttributes)
    }
}
