package projektor.testcase

import projektor.server.api.TestCase
import projektor.server.api.attachments.Attachment

class CypressVideoAttachmentMatcher : TestCaseAttachmentMatcher {
    override fun findAttachment(
        testCase: TestCase,
        attachments: List<Attachment>?,
    ): Attachment? =
        attachments?.find { attachment ->
            val testCaseFilePathParts = testCase.fileName?.split("/")

            if (testCaseFilePathParts?.isNotEmpty() == true) {
                val testCaseFileName = testCaseFilePathParts.last()

                attachment.fileName == "$testCaseFileName.mp4"
            } else {
                false
            }
        }
}
