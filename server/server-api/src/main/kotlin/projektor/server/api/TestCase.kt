package projektor.server.api

import projektor.server.api.attachments.Attachment
import java.math.BigDecimal
import java.time.LocalDateTime

data class TestCase(
    val idx: Int,
    val testSuiteIdx: Int,
    val name: String,
    val packageName: String?,
    val testSuiteName: String?,
    val className: String?,
    val fileName: String?,
    val duration: BigDecimal?,
    val passed: Boolean,
    val skipped: Boolean,
    val hasSystemOutTestCase: Boolean,
    val hasSystemErrTestCase: Boolean,
    val hasSystemOutTestSuite: Boolean,
    val hasSystemErrTestSuite: Boolean,
    val publicId: String,
    val createdTimestamp: LocalDateTime,
    val failure: TestFailure?,
) {
    val fullName: String
        get() = """${packageName?.let { "$it."} ?: ""}$className.$name"""

    val hasSystemOut: Boolean
        get() = hasSystemOutTestCase || hasSystemOutTestSuite

    val hasSystemErr: Boolean
        get() = hasSystemErrTestCase || hasSystemErrTestSuite

    var attachments: List<Attachment>? = null
}
