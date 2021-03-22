package projektor.testcase

import projektor.server.api.TestCase
import projektor.server.api.attachments.Attachment

class CypressScreenshotAttachmentMatcher : TestCaseAttachmentMatcher {
    override fun findAttachment(testCase: TestCase, attachments: List<Attachment>?): Attachment? =
        attachments?.find { attachment ->
            val testCaseNameWords = testCase.name.split(" ")

            testCaseNameWords.all { word -> attachment.fileName.contains(word) }
        }
}
