package projektor.testcase

import io.ktor.util.*
import projektor.attachment.AttachmentService
import projektor.server.api.PublicId
import projektor.server.api.TestCase
import projektor.server.api.TestOutput

@KtorExperimentalAPI
class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val attachmentService: AttachmentService?
) {

    suspend fun fetchTestCase(testRunPublicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestCase? =
        testCaseRepository.fetchTestCase(testRunPublicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchFailedTestCases(publicId: PublicId): List<TestCase> {
        val failedTestCases = testCaseRepository.fetchFailedTestCases(publicId)

        if (failedTestCases.isNotEmpty()) {
            val attachments = attachmentService?.listAttachments(publicId)

            failedTestCases.forEach { testCase ->
                val testCaseAttachment = attachments?.find { attachment ->
                    val testCaseNameWords = testCase.name.split(" ")

                    testCaseNameWords.all { word -> attachment.fileName.contains(word) }
                }

                if (testCaseAttachment != null) {
                    testCase.attachments = listOf(testCaseAttachment)
                }
            }
        }

        return failedTestCases
    }

    suspend fun fetchSlowTestCases(publicId: PublicId, limit: Int): List<TestCase> =
        testCaseRepository.fetchSlowTestCases(publicId, limit)

    suspend fun fetchTestCaseSystemErr(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        testCaseRepository.fetchTestCaseSystemErr(publicId, testSuiteIdx, testCaseIdx)

    suspend fun fetchTestCaseSystemOut(publicId: PublicId, testSuiteIdx: Int, testCaseIdx: Int): TestOutput =
        testCaseRepository.fetchTestCaseSystemOut(publicId, testSuiteIdx, testCaseIdx)
}
