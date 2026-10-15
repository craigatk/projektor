package projektor.server.api.pullrequest

import projektor.server.api.coverage.CoverageGroup
import projektor.server.api.coverage.CoverageStats
import java.math.BigDecimal

data class PullRequestCoverageDelta(
    val testRunPublicId: String,
    // Most recent mainline test run with coverage before this one, which the deltas are relative to
    val baselineTestRunPublicId: String?,
    val overallStats: CoverageStats,
    val groups: List<CoverageGroup>,
    // Files whose line coverage changed versus the baseline (or are new), biggest drops first
    val changedFiles: List<CoverageFileDelta>,
    val changedFilesTruncated: Boolean,
)

data class CoverageFileDelta(
    val groupName: String,
    val filePath: String,
    val lineCoveredPercentage: BigDecimal,
    // Null when the file has no coverage in the baseline run
    val lineCoveredPercentageDelta: BigDecimal?,
    val missedLines: List<Int>,
    val partialLines: List<Int>,
)
