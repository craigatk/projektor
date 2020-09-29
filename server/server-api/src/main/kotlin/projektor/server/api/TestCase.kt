package projektor.server.api

import java.math.BigDecimal
import java.time.LocalDateTime

data class TestCase(
    val idx: Int,
    val testSuiteIdx: Int,
    val name: String,
    val packageName: String?,
    val className: String,
    val duration: BigDecimal,
    val passed: Boolean,
    val skipped: Boolean,
    val hasSystemOut: Boolean,
    val hasSystemErr: Boolean,
    val publicId: String,
    val createdTimestamp: LocalDateTime,
    val failure: TestFailure?
) {
    val fullName: String
        get() = """${packageName?.let { "$it."} ?: ""}$className.$name"""
}
