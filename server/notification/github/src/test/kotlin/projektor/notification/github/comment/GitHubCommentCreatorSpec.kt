package projektor.notification.github.comment

import io.kotest.core.spec.style.StringSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class GitHubCommentCreatorSpec : StringSpec() {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        "should create comment without project or coverage" {
            val createdDate = LocalDateTime.of(
                LocalDate.of(2020, 12, 16),
                LocalTime.of(14, 30)
            )

            val report = ReportCommentData(
                serverBaseUrl = "https://projektorlive.herokuapp.com/",
                publicId = "V1BMYK93MTNR",
                createdDate = createdDate,
                passed = true,
                failedTestCount = 0,
                totalTestCount = 25,
                coverage = null,
                project = null
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
Projektor reports:

| Projektor report | Result | Tests | Coverage | Project | Date | 
| ---------------- | ------ | ----- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [25 total](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/all) | | | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }
    }
}
