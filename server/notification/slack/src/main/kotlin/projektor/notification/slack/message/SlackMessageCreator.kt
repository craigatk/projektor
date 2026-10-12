package projektor.notification.slack.message

object SlackMessageCreator {
    private const val MAX_FLAKY_TESTS_LISTED = 10

    fun createMessage(
        channel: String,
        data: SlackNotificationData,
    ): SlackMessage {
        val summary = createSummary(data)

        val blocks =
            listOfNotNull(
                SlackBlock(type = "header", text = SlackText(type = "plain_text", text = summary)),
                data.mainlineFailure?.let { markdownSection(createMainlineFailureText(data, it)) },
                data.coverageDrop?.let { markdownSection(createCoverageDropText(data, it)) },
                data.newFlakyTests?.let { markdownSection(createNewFlakyTestsText(data, it)) },
                markdownSection(createContextText(data)),
            )

        return SlackMessage(channel = channel, text = summary, blocks = blocks)
    }

    fun createChannelConfiguredMessage(
        channel: String,
        repoName: String,
        projectName: String?,
    ): SlackMessage =
        SlackMessage(
            channel = channel,
            text = "Projektor will post mainline notifications for ${repoAndProject(repoName, projectName)} to this channel.",
        )

    private fun createSummary(data: SlackNotificationData): String {
        val triggers =
            listOfNotNull(
                data.mainlineFailure?.let { "mainline is failing" },
                data.coverageDrop?.let { "coverage dropped" },
                data.newFlakyTests?.let { if (it.tests.size == 1) "new flaky test" else "new flaky tests" },
            )

        return "${repoAndProject(data.repoName, data.projectName)}: ${triggers.joinToString(", ")}"
    }

    private fun createMainlineFailureText(
        data: SlackNotificationData,
        mainlineFailure: MainlineFailureData,
    ): String =
        ":red_circle: *Mainline went red* - " +
            "<${createReportLink(data, "failed")}|${mainlineFailure.failedTestCount} failed> " +
            "of ${mainlineFailure.totalTestCount} tests"

    private fun createCoverageDropText(
        data: SlackNotificationData,
        coverageDrop: CoverageDropData,
    ): String =
        ":chart_with_downwards_trend: *Coverage dropped* - " +
            "<${createReportLink(data, "coverage")}|${coverageDrop.lineCoveredPercentage.toPlainString()}%> " +
            "(${coverageDrop.lineCoverageDelta.toPlainString()}%)"

    private fun createNewFlakyTestsText(
        data: SlackNotificationData,
        newFlakyTests: NewFlakyTestsData,
    ): String {
        val listedTests =
            newFlakyTests.tests.take(MAX_FLAKY_TESTS_LISTED).joinToString("\n") { test ->
                "• `${test.name}` - failed ${test.failureCount} times (${test.failurePercentage.toPlainString()}% of recent runs)"
            }

        val remainingCount = newFlakyTests.tests.size - MAX_FLAKY_TESTS_LISTED
        val remainingText = if (remainingCount > 0) "\n…and $remainingCount more" else ""

        return ":game_die: *<${createRepositoryLink(data, "tests/flaky")}|New flaky tests>*\n$listedTests$remainingText"
    }

    private fun createContextText(data: SlackNotificationData): String {
        val parts =
            listOfNotNull(
                "<${createReportLink(data, "")}|Projektor report>",
                data.branchName?.let { "branch `$it`" },
                data.commitSha?.let { "commit `${it.take(7)}`" },
            )

        return parts.joinToString(" · ")
    }

    private fun markdownSection(text: String) = SlackBlock(type = "section", text = SlackText(type = "mrkdwn", text = text))

    private fun repoAndProject(
        repoName: String,
        projectName: String?,
    ): String = if (projectName.isNullOrEmpty()) repoName else "$repoName ($projectName)"

    private fun baseUrl(data: SlackNotificationData): String = data.projektorServerBaseUrl.trimEnd('/')

    private fun createReportLink(
        data: SlackNotificationData,
        uri: String,
    ): String = "${baseUrl(data)}/tests/${data.publicId}/$uri"

    private fun createRepositoryLink(
        data: SlackNotificationData,
        uri: String,
    ): String {
        val projectPart = if (data.projectName.isNullOrEmpty()) "" else "/project/${data.projectName}"

        return "${baseUrl(data)}/repository/${data.repoName}$projectPart/$uri"
    }
}
