package projektor.testcase

import org.junit.jupiter.api.Test
import projektor.server.api.history.TestCaseHistoryEntry
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.math.BigDecimal
import java.time.LocalDateTime

class TestCaseHistoryBuilderTest {
    @Test
    fun `when requested run passed should not have first failure`() {
        val history = buildTestCaseHistory(listOf(entry("1", passed = true), entry("2")))

        expectThat(history.firstFailure).isNull()
        expectThat(history.lastPassedBeforeFailure).isNull()
    }

    @Test
    fun `skipped runs should not break the failure streak`() {
        val history =
            buildTestCaseHistory(
                listOf(
                    entry("1"),
                    entry("2", passed = true, skipped = true),
                    entry("3"),
                    entry("4", passed = true),
                    entry("5"),
                ),
            )

        expectThat(history.firstFailure).isNotNull().get { publicId }.isEqualTo("3")
        expectThat(history.lastPassedBeforeFailure).isNotNull().get { publicId }.isEqualTo("4")
    }

    @Test
    fun `when test has always failed should not have last passed run`() {
        val history = buildTestCaseHistory(listOf(entry("1"), entry("2")))

        expectThat(history.firstFailure).isNotNull().get { publicId }.isEqualTo("2")
        expectThat(history.lastPassedBeforeFailure).isNull()
    }

    @Test
    fun `when no history should be empty`() {
        val history = buildTestCaseHistory(listOf())

        expectThat(history.firstFailure).isNull()
    }

    private fun entry(
        publicId: String,
        passed: Boolean = false,
        skipped: Boolean = false,
    ) = TestCaseHistoryEntry(
        publicId = publicId,
        testSuiteIdx = 1,
        testCaseIdx = 1,
        createdTimestamp = LocalDateTime.now(),
        passed = passed,
        skipped = skipped,
        duration = BigDecimal.ONE,
        branchName = "main",
        commitSha = null,
        pullRequestNumber = null,
    )
}
