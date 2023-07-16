package projektor.notification.github.comment

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.TimeZone

class GitHubCommentCreatorSpec : StringSpec() {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        "should create comment for passing build without project or coverage" {
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "my-org",
                    repoName = "my-repo",
                    branchName = "my-branch"
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
                performance = null,
                project = null
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [25 total](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/all) |  |  | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }

        "should create comment for passing build with project" {
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "my-org",
                    repoName = "my-repo",
                    branchName = "my-branch"
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
                performance = null,
                project = "my-project"
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [25 total](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/all) |  | my-project | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }

        "should create comment for failing build" {
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "my-org",
                    repoName = "my-repo",
                    branchName = "my-branch"
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = false,
                failedTestCount = 5,
                totalTestCount = 30,
                coverage = null,
                performance = null,
                project = null
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Failed | [5 failed](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/failed) / [30 total](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/all) |  |  | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }

        forAll(
            row(BigDecimal("91.05"), BigDecimal("2.41"), "91.05% (+2.41%)"),
            row(BigDecimal("82.15"), BigDecimal("-5.25"), "82.15% (-5.25%)"),
            row(BigDecimal("83.43"), null, "83.43%")
        ) { coveredPercentage, coverageDelta, expectedCoverageCell ->
            "should create comment for build with coverage $coveredPercentage and delta $coverageDelta" {
                val report = ReportCommentData(
                    projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                    git = ReportCommentGitData(
                        orgName = "my-org",
                        repoName = "my-repo",
                        branchName = "my-branch"
                    ),
                    publicId = "V1BMYK93MTNR",
                    createdDate = LocalDateTime.of(
                        LocalDate.of(2020, 12, 16),
                        LocalTime.of(14, 30)
                    ),
                    passed = true,
                    failedTestCount = 0,
                    totalTestCount = 25,
                    coverage = ReportCoverageCommentData(
                        lineCoveredPercentage = coveredPercentage,
                        lineCoverageDelta = coverageDelta
                    ),
                    performance = null,
                    project = null
                )

                val commentText = GitHubCommentCreator.createComment(report)

                expectThat(commentText).isEqualTo(
                    """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [25 total](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/all) | [$expectedCoverageCell](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/coverage) |  | 2020-12-16 02:30 PM UTC |
                    """.trimIndent().trim()
                )
            }
        }

        "should create comment with one performance result" {
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "my-org",
                    repoName = "my-repo",
                    branchName = "my-branch"
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 0,
                coverage = null,
                performance = listOf(
                    ReportCommentPerformanceData(
                        name = "performance.json",
                        p95 = BigDecimal("59.13"),
                        requestsPerSecond = BigDecimal("1941.79")
                    )
                ),
                project = null
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [p95: 59 ms, RPS: 1942](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) |  |  | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }

        "should create comment with two performance results" {
            val report = ReportCommentData(
                projektorServerBaseUrl = "https://projektorlive.herokuapp.com/",
                git = ReportCommentGitData(
                    orgName = "my-org",
                    repoName = "my-repo",
                    branchName = "my-branch"
                ),
                publicId = "V1BMYK93MTNR",
                createdDate = LocalDateTime.of(
                    LocalDate.of(2020, 12, 16),
                    LocalTime.of(14, 30)
                ),
                passed = true,
                failedTestCount = 0,
                totalTestCount = 0,
                coverage = null,
                performance = listOf(
                    ReportCommentPerformanceData(
                        name = "performance1.json",
                        p95 = BigDecimal("59.13"),
                        requestsPerSecond = BigDecimal("1941.79")
                    ),
                    ReportCommentPerformanceData(
                        name = "performance2.json",
                        p95 = BigDecimal("79.13"),
                        requestsPerSecond = BigDecimal("1945.79")
                    )
                ),
                project = null
            )

            val commentText = GitHubCommentCreator.createComment(report)

            expectThat(commentText).isEqualTo(
                """
**Projektor reports**

| Projektor report | Result | Tests executed | Coverage | Project | Date | 
| ---------------- | ------ | -------------- | -------- | ------- | ---- |
| [Projektor report](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) | Passed | [performance1.json - p95: 59 ms, RPS: 1942<br />performance2.json - p95: 79 ms, RPS: 1946](https://projektorlive.herokuapp.com/tests/V1BMYK93MTNR/) |  |  | 2020-12-16 02:30 PM UTC |
                """.trimIndent().trim()
            )
        }
    }
}
