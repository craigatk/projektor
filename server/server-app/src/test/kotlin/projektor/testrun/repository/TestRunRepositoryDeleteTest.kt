package projektor.testrun.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseCleanupLock
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isNull
import java.time.LocalDate

@DatabaseCleanupLock(DatabaseCleanupLock.Mode.CREATES_CLEANUP_ELIGIBLE_TEST_RUNS)
abstract class TestRunRepositoryDeleteTest : RepositoryTestCase() {
    @Test
    fun `should delete test run`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(
            publicId,
            listOf(
                TestSuiteData(
                    "testSuite1",
                    listOf("testSuite1PassedTestCase1", "testSuite1PassedTestCase2"),
                    listOf("testSuite1FailedTestCase1", "testSuite1FailedTestCase2"),
                    listOf(),
                ),
                TestSuiteData(
                    "testSuite2",
                    listOf("testSuite2PassedTestCase1", "testSuite2PassedTestCase2"),
                    listOf("testSuite2FailedTestCase1"),
                    listOf(),
                ),
            ),
        )

        expectThat(runBlocking { testCaseRepository.fetchTestCases(publicId) }).hasSize(7)

        runBlocking { testRunRepository.deleteTestRun(publicId) }

        expectThat(runBlocking { testRunRepository.fetchTestRun(publicId) }).isNull()
        expectThat(runBlocking { testRunRepository.fetchTestRunSummary(publicId) }).isNull()
        expectThat(runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }).isEmpty()
        expectThat(runBlocking { testCaseRepository.fetchTestCases(publicId) }).isEmpty()
    }

    @Test
    fun `should find test runs created before a given date that are not pinned to delete`() {
        val tooNewPublicId = randomPublicId()
        testRunGenerator.createTestRun(tooNewPublicId, LocalDate.of(2020, 4, 1), false)

        val pinnedPublicId = randomPublicId()
        testRunGenerator.createTestRun(pinnedPublicId, LocalDate.of(2020, 2, 1), true)

        val shouldDelete1PublicId = randomPublicId()
        testRunGenerator.createTestRun(shouldDelete1PublicId, LocalDate.of(2020, 2, 1), false)

        val shouldDelete2PublicId = randomPublicId()
        testRunGenerator.createTestRun(shouldDelete2PublicId, LocalDate.of(2020, 2, 1), false)

        val testRunsToDelete = runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.of(2020, 2, 2)) }

        expectThat(testRunsToDelete)
            .contains(shouldDelete1PublicId, shouldDelete2PublicId)
            .doesNotContain(tooNewPublicId, pinnedPublicId)
    }

    @Test
    fun `should find test runs created before a given date that are not pinned and have attachments`() {
        val tooNewPublicId = randomPublicId()
        testRunGenerator.createTestRun(tooNewPublicId, LocalDate.of(2020, 4, 1), false)
        testRunGenerator.addAttachment(tooNewPublicId, "attachment", "attachment.txt")

        val pinnedPublicId = randomPublicId()
        testRunGenerator.createTestRun(pinnedPublicId, LocalDate.of(2020, 2, 1), true)
        testRunGenerator.addAttachment(pinnedPublicId, "attachment", "attachment.txt")

        val shouldFetch1PublicId = randomPublicId()
        testRunGenerator.createTestRun(shouldFetch1PublicId, LocalDate.of(2020, 2, 1), false)
        testRunGenerator.addAttachment(shouldFetch1PublicId, "attachment", "attachment.txt")

        val shouldFetch2PublicId = randomPublicId()
        testRunGenerator.createTestRun(shouldFetch2PublicId, LocalDate.of(2020, 2, 1), false)
        testRunGenerator.addAttachment(shouldFetch2PublicId, "attachment", "attachment.txt")

        val noAttachmentsPublicId = randomPublicId()
        testRunGenerator.createTestRun(noAttachmentsPublicId, LocalDate.of(2020, 2, 1), false)

        val fetchedTestRuns =
            runBlocking { testRunRepository.findTestRunsCreatedBeforeAndNotPinnedWithAttachments(LocalDate.of(2020, 2, 2)) }

        expectThat(fetchedTestRuns)
            .contains(shouldFetch1PublicId, shouldFetch2PublicId)
            .doesNotContain(tooNewPublicId, pinnedPublicId, noAttachmentsPublicId)
    }
}

class TestRunRepositoryDeleteDatabaseTest : TestRunRepositoryDeleteTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunRepositoryDeleteObjectStoreTest : TestRunRepositoryDeleteTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
