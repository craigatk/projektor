package projektor.cleanup

import io.ktor.util.KtorExperimentalAPI
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.get
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.attachment.AttachmentConfig
import projektor.attachment.AttachmentDatabaseRepository
import projektor.attachment.AttachmentService
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNull

@KtorExperimentalAPI
class CleanupServiceTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should delete non-grouped test run without attachments`() {
        val cleanupService = CleanupService(get(), null)

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

        runBlocking { cleanupService.cleanupTestRun(publicId) }

        expectThat(testRunDao.fetchOneById(testRun.id)).isNull()

        testSuiteIds.forEach {
            expectThat(testSuiteDao.fetchOneById(it)).isNull()
        }

        testCaseIds.forEach {
            expectThat(testCaseDao.fetchOneById(it)).isNull()
        }
    }

    @Test
    fun `should delete grouped test run without attachments`() {
        val cleanupService = CleanupService(get(), null)

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

        val testGroup1 = testRunDBGenerator.addTestSuiteGroupToTestRun("group1", testRun, listOf("testSuite1"))
        val testGroup2 = testRunDBGenerator.addTestSuiteGroupToTestRun("group2", testRun, listOf("testSuite2"))

        val testSuiteIds = testSuiteDao.fetchByTestRunId(testRun.id).map { it.id }
        val testCaseIds = testSuiteIds.flatMap { testCaseDao.fetchByTestSuiteId(it) }.map { it.id }

        runBlocking { cleanupService.cleanupTestRun(publicId) }

        expectThat(testRunDao.fetchOneById(testRun.id)).isNull()

        testSuiteIds.forEach {
            expectThat(testSuiteDao.fetchOneById(it)).isNull()
        }

        testCaseIds.forEach {
            expectThat(testCaseDao.fetchOneById(it)).isNull()
        }

        expectThat(testSuiteGroupDao.fetchOneById(testGroup1.id)).isNull()
        expectThat(testSuiteGroupDao.fetchOneById(testGroup2.id)).isNull()
    }

    @Test
    fun `should delete test run with attachments`() {
        val attachmentsConfig = AttachmentConfig(
                "http://localhost:9000",
                "attachmentsremoving",
                true,
                "minio_access_key",
                "minio_secret_key",
                null
        )

        val attachmentService = AttachmentService(attachmentsConfig, AttachmentDatabaseRepository(dslContext))
        attachmentService.conditionallyCreateBucketIfNotExists()

        val cleanupService = CleanupService(get(), attachmentService)

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

        val attachmentInputStream = File("src/test/resources/test-attachment.txt").inputStream()
        val attachmentFileNames = listOf("attachment1.txt", "attachment2.txt", "attachment3.txt")
        attachmentFileNames.forEach { runBlocking { attachmentService.addAttachment(publicId, it, attachmentInputStream, null) } }

        val testSuiteIds = testSuiteDao.fetchByTestRunId(testRun.id).map { it.id }
        val testCaseIds = testSuiteIds.flatMap { testCaseDao.fetchByTestSuiteId(it) }.map { it.id }

        runBlocking { cleanupService.cleanupTestRun(publicId) }

        expectThat(testRunDao.fetchOneById(testRun.id)).isNull()

        testSuiteIds.forEach {
            expectThat(testSuiteDao.fetchOneById(it)).isNull()
        }

        testCaseIds.forEach {
            expectThat(testCaseDao.fetchOneById(it)).isNull()
        }

        expectThat(attachmentDao.fetchByTestRunPublicId(publicId.id)).hasSize(0)

        attachmentFileNames.forEach { attachmentFileName ->
            val attachmentAfterCleanup = runBlocking { attachmentService.getAttachment(publicId, attachmentFileName) }
            expectThat(attachmentAfterCleanup).isNull()
        }
    }
}
