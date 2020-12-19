package projektor.notification.github.comment

import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import projektor.notification.github.GitHubClientConfig
import projektor.notification.github.auth.JwtProvider
import projektor.notification.github.auth.JwtTokenConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Ignored
class GitHubCommentServiceLiveSpec : StringSpec() {
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

    private val jwtProvider = JwtProvider(jwtTokenConfig)

    init {
        "should create comment when one does not exist" {
            val commentService = GitHubCommentService(GitHubCommentClient(clientConfig, jwtProvider))

            val commentData = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "craigatk",
                    repoName = "projektor",
                    branchName = "test-pr"
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                project = null
            )

            commentService.upsertReportComment(commentData)
        }

        "should update comment when one already exists" {
            val commentService = GitHubCommentService(GitHubCommentClient(clientConfig, jwtProvider))

            val newCommentData = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "craigatk",
                    repoName = "projektor",
                    branchName = "test-pr"
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 17),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 30,
                coverage = null,
                project = null
            )

            commentService.upsertReportComment(newCommentData)
        }
    }

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
