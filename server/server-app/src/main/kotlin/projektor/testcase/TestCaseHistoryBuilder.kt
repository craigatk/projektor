package projektor.testcase

import projektor.server.api.history.TestCaseHistory
import projektor.server.api.history.TestCaseHistoryEntry

/**
 * Builds the test case history from entries ordered most recent first,
 * where the first entry is the requested test run.
 *
 * When the requested run failed, walks back through older runs to find where the
 * current streak of failures started. Skipped runs don't break the streak.
 */
fun buildTestCaseHistory(entries: List<TestCaseHistoryEntry>): TestCaseHistory {
    val requestedEntry = entries.firstOrNull()

    if (requestedEntry == null || !requestedEntry.failed) {
        return TestCaseHistory(entries, firstFailure = null, lastPassedBeforeFailure = null)
    }

    var firstFailure = requestedEntry
    var lastPassedBeforeFailure: TestCaseHistoryEntry? = null

    for (entry in entries.drop(1)) {
        if (entry.skipped) {
            continue
        } else if (entry.passed) {
            lastPassedBeforeFailure = entry
            break
        } else {
            firstFailure = entry
        }
    }

    return TestCaseHistory(entries, firstFailure, lastPassedBeforeFailure)
}
