package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.testrun.TestRunDatabaseRepository
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.isNull
import java.time.LocalDate

class TestRunDatabaseRepositoryDeleteTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should delete test run`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        val testRun = testRunDBGenerator.createTestRun(
            publicId,
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
        testRunDBGenerator.createTestRun(tooNewPublicId, LocalDate.of(2020, 4, 1), false)

        val pinnedPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(pinnedPublicId, LocalDate.of(2020, 2, 1), true)

        val shouldDelete1PublicId = randomPublicId()
        testRunDBGenerator.createTestRun(shouldDelete1PublicId, LocalDate.of(2020, 2, 1), false)

        val shouldDelete2PublicId = randomPublicId()
        testRunDBGenerator.createTestRun(shouldDelete2PublicId, LocalDate.of(2020, 2, 1), false)

        val testRunsToDelete = runBlocking { testRunDatabaseRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.of(2020, 2, 2)) }

        expectThat(testRunsToDelete)
            .contains(shouldDelete1PublicId, shouldDelete2PublicId)
            .doesNotContain(tooNewPublicId, pinnedPublicId)
    }

    @Test
    fun `should find test runs created before a given date that are not pinned and have attachments`() {
        val testRunDatabaseRepository = TestRunDatabaseRepository(dslContext)

        val tooNewPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(tooNewPublicId, LocalDate.of(2020, 4, 1), false)
        testRunDBGenerator.addAttachment(tooNewPublicId, "attachment", "attachment.txt")

        val pinnedPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(pinnedPublicId, LocalDate.of(2020, 2, 1), true)
        testRunDBGenerator.addAttachment(pinnedPublicId, "attachment", "attachment.txt")

        val shouldFetch1PublicId = randomPublicId()
        testRunDBGenerator.createTestRun(shouldFetch1PublicId, LocalDate.of(2020, 2, 1), false)
        testRunDBGenerator.addAttachment(shouldFetch1PublicId, "attachment", "attachment.txt")

        val shouldFetch2PublicId = randomPublicId()
        testRunDBGenerator.createTestRun(shouldFetch2PublicId, LocalDate.of(2020, 2, 1), false)
        testRunDBGenerator.addAttachment(shouldFetch2PublicId, "attachment", "attachment.txt")

        val noAttachmentsPublicId = randomPublicId()
        testRunDBGenerator.createTestRun(noAttachmentsPublicId, LocalDate.of(2020, 2, 1), false)

        val fetchedTestRuns = runBlocking { testRunDatabaseRepository.findTestRunsCreatedBeforeAndNotPinnedWithAttachments(LocalDate.of(2020, 2, 2)) }

        expectThat(fetchedTestRuns)
            .contains(shouldFetch1PublicId, shouldFetch2PublicId)
            .doesNotContain(tooNewPublicId, pinnedPublicId, noAttachmentsPublicId)
    }
}
