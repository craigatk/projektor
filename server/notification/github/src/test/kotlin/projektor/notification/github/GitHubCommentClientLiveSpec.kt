package projektor.notification.github

import io.kotest.core.spec.style.StringSpec
import projektor.notification.github.auth.JwtProvider
import projektor.notification.github.auth.JwtTokenConfig
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class GitHubCommentClientLiveSpec : StringSpec() {
    private val keyContents = loadTextFromFile("projektor-test-reports.2020-12-12.private-key.pem")
    private val ttl: Long = 60000
    private val gitHubAppId = "91621"
    private val gitHubApiUrl = "https://api.github.com"

    private val clientConfig = GitHubClientConfig(
        gitHubApiUrl = gitHubApiUrl
    )

    private val jwtTokenConfig = JwtTokenConfig(
        gitHubAppId = gitHubAppId,
        pemContents = keyContents,
        ttlMillis = ttl
    )

    private val jwtCreator = JwtProvider(jwtTokenConfig)

    init {
        "!should create comment on live issue" {
            val commentClient = GitHubCommentClient(clientConfig, jwtCreator)

            val issueId = 241
            val commentText = "Here is a test comment from Projektor"

            val repository = commentClient.getRepository("craigatk", "projektor")
            commentClient.addComment(repository, issueId, commentText)
        }

        "!should find open PR in branch" {
            val commentClient = GitHubCommentClient(clientConfig, jwtCreator)

            val branchName = "test-pr"
            val expectedPrNumber = 249

            val repository = commentClient.getRepository("craigatk", "projektor")
            val prNumber = commentClient.findOpenPullRequestsForBranch(repository, branchName)
            expectThat(prNumber).isNotNull().isEqualTo(expectedPrNumber)
        }

        "!should find comment in PR with text" {
            val commentClient = GitHubCommentClient(clientConfig, jwtCreator)

            val commentText = "Projektor reports"
            val prNumber = 249

            val repository = commentClient.getRepository("craigatk", "projektor")

            val comment = commentClient.findCommentWithText(repository, prNumber, commentText)
            expectThat(comment).isNotNull()
        }

        "!should update comment in PR" {
            val commentClient = GitHubCommentClient(clientConfig, jwtCreator)

            val commentText = "Comment to update"
            val prNumber = 249

            val repository = commentClient.getRepository("craigatk", "projektor")

            commentClient.addComment(repository, prNumber, commentText)

            val comment = commentClient.findCommentWithText(repository, prNumber, commentText)
            expectThat(comment).isNotNull()

            commentClient.updateComment(comment!!, "Comment to update!")
        }
    }

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
