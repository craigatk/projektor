package projektor.notification.slack.message

import java.math.BigDecimal

data class SlackNotificationData(
    val projektorServerBaseUrl: String,
    val publicId: String,
    val repoName: String,
    val projectName: String?,
    val branchName: String?,
    val commitSha: String?,
    val mainlineFailure: MainlineFailureData?,
    val coverageDrop: CoverageDropData?,
    val newFlakyTests: NewFlakyTestsData?,
)

data class MainlineFailureData(val failedTestCount: Int, val totalTestCount: Int)

data class CoverageDropData(val lineCoveredPercentage: BigDecimal, val lineCoverageDelta: BigDecimal)

data class NewFlakyTestsData(val tests: List<FlakyTestData>)

data class FlakyTestData(val name: String, val failureCount: Int, val failurePercentage: BigDecimal)
