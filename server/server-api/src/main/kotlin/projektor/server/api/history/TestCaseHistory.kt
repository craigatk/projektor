package projektor.server.api.history

import java.math.BigDecimal
import java.time.LocalDateTime

data class TestCaseHistory(
    // Most recent run first, starting with the requested test run
    val entries: List<TestCaseHistoryEntry>,
    // Earliest run in the current unbroken streak of failures (null if the requested run didn't fail)
    val firstFailure: TestCaseHistoryEntry?,
    // Most recent passing run before the current streak of failures
    val lastPassedBeforeFailure: TestCaseHistoryEntry?,
)

data class TestCaseHistoryEntry(
    val publicId: String,
    val testSuiteIdx: Int,
    val testCaseIdx: Int,
    val createdTimestamp: LocalDateTime,
    val passed: Boolean,
    val skipped: Boolean,
    val duration: BigDecimal?,
    val branchName: String?,
    val commitSha: String?,
    val pullRequestNumber: Int?,
) {
    val failed: Boolean
        get() = !passed && !skipped
}
