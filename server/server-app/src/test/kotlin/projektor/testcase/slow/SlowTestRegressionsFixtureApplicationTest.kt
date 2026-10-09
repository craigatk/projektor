package projektor.testcase.slow

import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.slow.SlowTestRegression
import projektor.server.api.slow.SlowTestRegressions
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.math.BigDecimal

class SlowTestRegressionsFixtureApplicationTest : ApplicationTestCase() {
    @Test
    fun `should flag the slower tests from the duration regression fixtures`() =
        projektorTestApplication {
            val gitMetadata = GitMetadata()
            gitMetadata.repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"
            gitMetadata.branchName = "main"
            gitMetadata.isMainBranch = true
            val resultsMetadata = ResultsMetadata()
            resultsMetadata.git = gitMetadata
            resultsMetadata.ci = true

            val resultsXmlLoader = ResultsXmlLoader()
            val groupedResultsXmlLoader = GroupedResultsXmlLoader()

            repeat(3) {
                val response =
                    client.postGroupedResultsJSON(
                        groupedResultsXmlLoader.wrapResultsXmlInGroup(
                            resultsXmlLoader.durationRegressionBaseline(),
                            metadata = resultsMetadata,
                        ),
                    )
                waitForTestRunSaveToComplete(response)
            }
            val regressedResponse =
                client.postGroupedResultsJSON(
                    groupedResultsXmlLoader.wrapResultsXmlInGroup(
                        resultsXmlLoader.durationRegressionRegressed(),
                        metadata = resultsMetadata,
                    ),
                )
            val (regressedPublicId, _) = waitForTestRunSaveToComplete(regressedResponse)

            val result =
                objectMapper.readValue(
                    client.get("/run/${regressedPublicId.id}/cases/slow/regressions").bodyAsText(),
                    SlowTestRegressions::class.java,
                )

            expectThat(result.baselineRunCount).isEqualTo(3)
            expectThat(result.regressions)
                .map { it.testCase.name }
                .containsExactly("should save order", "should load order history")
            expectThat(result.regressions)
                .map(SlowTestRegression::increasePercent)
                .containsExactly(BigDecimal("300.0"), BigDecimal("60.0"))
        }
}
