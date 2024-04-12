package projektor.testcase

import projektor.server.api.TestCase
import projektor.server.api.attachments.Attachment

interface TestCaseAttachmentMatcher {
    fun findAttachment(
        testCase: TestCase,
        attachments: List<Attachment>?,
    ): Attachment?
}
