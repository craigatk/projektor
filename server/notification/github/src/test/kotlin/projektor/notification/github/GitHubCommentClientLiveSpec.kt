package projektor.notification.github

import io.kotest.core.spec.style.StringSpec

class GitHubCommentClientLiveSpec : StringSpec() {
    init {
        "!should create comment on live issue" {
            val keyContents = loadTextFromFile("projektor-test-reports.2020-12-12.private-key.pem")
            val ttl: Long = 60000
            val gitHubAppId = "91621"
            val gitHubApiUrl = "https://api.github.com"

            val clientConfig = GitHubClientConfig(
                gitHubApiUrl = gitHubApiUrl
            )

            val jwtTokenConfig = JwtTokenConfig(
                gitHubAppId = gitHubAppId,
                pemContents = keyContents,
                ttlMillis = ttl
            )

            val jwtCreator = JwtProvider(jwtTokenConfig)

            val commentClient = GitHubCommentClient(clientConfig, jwtCreator)

            val issueId = 241
            val commentText = "Here is a test comment from Projektor"

            commentClient.addComment("craigatk", "projektor", issueId, commentText)
        }
    }

    private fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()
}
