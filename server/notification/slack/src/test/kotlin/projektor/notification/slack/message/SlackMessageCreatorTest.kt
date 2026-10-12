package projektor.notification.slack.message

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.math.BigDecimal

class SlackMessageCreatorTest {
    private val baseData =
        SlackNotificationData(
            projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
            publicId = "V1BMYK93MTNR",
            repoName = "my-org/my-repo",
            projectName = null,
            branchName = "main",
            commitSha = "0123456789abcdef",
            mainlineFailure = null,
            coverageDrop = null,
            newFlakyTests = null,
        )

    private fun SlackMessage.blockTexts() = blocks.orEmpty().map { it.text.text }

    @Test
    fun `should create message when mainline goes red`() {
        val data = baseData.copy(mainlineFailure = MainlineFailureData(failedTestCount = 2, totalTestCount = 25))

        val message = SlackMessageCreator.createMessage("my-channel", data)

        expectThat(message.channel).isEqualTo("my-channel")
        expectThat(message.text).isEqualTo("my-org/my-repo: mainline is failing")
        expectThat(message.blockTexts()).isEqualTo(
            listOf(
                "my-org/my-repo: mainline is failing",
                ":red_circle: *Mainline went red* - " +
                    "<https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/failed|2 failed> of 25 tests",
                "<https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/|Projektor report> · branch `main` · commit `0123456`",
            ),
        )
    }

    @Test
    fun `should create message when coverage drops`() {
        val data =
            baseData.copy(
                coverageDrop = CoverageDropData(lineCoveredPercentage = BigDecimal("84.25"), lineCoverageDelta = BigDecimal("-1.50")),
            )

        val message = SlackMessageCreator.createMessage("my-channel", data)

        expectThat(message.text).isEqualTo("my-org/my-repo: coverage dropped")
        expectThat(message.blockTexts()).hasSize(3)
        expectThat(message.blockTexts()[1]).isEqualTo(
            ":chart_with_downwards_trend: *Coverage dropped* - " +
                "<https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/coverage|84.25%> (-1.50%)",
        )
    }

    @Test
    fun `should create message for new flaky tests in project`() {
        val data =
            baseData.copy(
                projectName = "ui",
                newFlakyTests =
                    NewFlakyTestsData(
                        listOf(
                            FlakyTestData(
                                name = "projektor.MySpec.should work",
                                failureCount = 5,
                                failurePercentage = BigDecimal("10.00"),
                            ),
                        ),
                    ),
            )

        val message = SlackMessageCreator.createMessage("my-channel", data)

        expectThat(message.text).isEqualTo("my-org/my-repo (ui): new flaky test")
        expectThat(message.blockTexts()[1]).isEqualTo(
            ":game_die: *<https://projektorlive.herokuapp.com/repository/my-org/my-repo/project/ui/tests/flaky|New flaky tests>*\n" +
                "• `projektor.MySpec.should work` - failed 5 times (10.00% of recent runs)",
        )
    }

    @Test
    fun `should cap the number of flaky tests listed`() {
        val tests = (1..12).map { FlakyTestData(name = "Spec.test$it", failureCount = 5, failurePercentage = BigDecimal("10.00")) }
        val data = baseData.copy(newFlakyTests = NewFlakyTestsData(tests))

        val message = SlackMessageCreator.createMessage("my-channel", data)

        expectThat(message.text).isEqualTo("my-org/my-repo: new flaky tests")
        expectThat(message.blockTexts()[1]).contains("Spec.test10").not().contains("Spec.test11")
        expectThat(message.blockTexts()[1]).contains("…and 2 more")
    }

    @Test
    fun `should combine all triggers into one message`() {
        val data =
            baseData.copy(
                branchName = null,
                commitSha = null,
                mainlineFailure = MainlineFailureData(failedTestCount = 1, totalTestCount = 10),
                coverageDrop = CoverageDropData(lineCoveredPercentage = BigDecimal("84.25"), lineCoverageDelta = BigDecimal("-1.50")),
                newFlakyTests =
                    NewFlakyTestsData(
                        listOf(FlakyTestData(name = "Spec.test", failureCount = 5, failurePercentage = BigDecimal("10.00"))),
                    ),
            )

        val message = SlackMessageCreator.createMessage("my-channel", data)

        expectThat(message.text).isEqualTo("my-org/my-repo: mainline is failing, coverage dropped, new flaky test")
        expectThat(message.blockTexts()).hasSize(5)
        expectThat(message.blockTexts().last()).isEqualTo("<https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/|Projektor report>")
    }

    @Test
    fun `should create channel configured message`() {
        val message = SlackMessageCreator.createChannelConfiguredMessage("my-channel", "my-org/my-repo", "ui")

        expectThat(message.channel).isEqualTo("my-channel")
        expectThat(message.text).contains("my-org/my-repo (ui)")
        expectThat(message.blocks).isNull()
    }
}
