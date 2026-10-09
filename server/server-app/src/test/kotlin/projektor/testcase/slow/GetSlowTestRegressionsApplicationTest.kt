package projektor.testcase.slow

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.tables.pojos.TestRun
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.server.api.slow.SlowTestRegressions
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class GetSlowTestRegressionsApplicationTest : ApplicationTestCase() {
    private val passingSuite =
        listOf(TestSuiteData("projektor.SlowSuite", listOf("other", "target"), listOf(), listOf()))

    private val failingSuite =
        listOf(TestSuiteData("projektor.SlowSuite", listOf("other"), listOf("target"), listOf()))

    @Test
    fun `should flag test cases slower than their baseline from passing runs`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            repeat(3) { createRunWithTargetDuration(repoName, "1.0") }
            // Failing runs aren't part of the baseline
            createRunWithTargetDuration(repoName, "100.0", suite = failingSuite)
            val requestedRun = randomPublicId()
            createRunWithTargetDuration(repoName, "3.0", publicId = requestedRun)

            val response = client.get("/run/${requestedRun.id}/cases/slow/regressions")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val result = objectMapper.readValue(response.bodyAsText(), SlowTestRegressions::class.java)

            expectThat(result.thresholdPercent).isEqualTo(50)
            expectThat(result.baselineRunCount).isEqualTo(4)
            expectThat(result.regressions).hasSize(1)
            expectThat(result.regressions[0]) {
                get { testCase.name }.isEqualTo("target")
                get { testCase.testSuiteIdx }.isEqualTo(1)
                get { testCase.idx }.isEqualTo(2)
                get { baselineDuration }.isEqualTo(BigDecimal("1.000"))
                get { baselineSampleCount }.isEqualTo(3)
                get { durationIncrease }.isEqualTo(BigDecimal("2.000"))
                get { increasePercent }.isEqualTo(BigDecimal("200.0"))
            }
        }

    @Test
    fun `should only use earlier CI runs on the same branch as the baseline`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            repeat(3) { createRunWithTargetDuration(repoName, "1.0") }
            createRunWithTargetDuration(repoName, "10.0", ci = false)
            createRunWithTargetDuration(repoName, "10.0", branchName = "feature")
            createRunWithTargetDuration("$repoName-other", "10.0")
            val requestedRun = randomPublicId()
            createRunWithTargetDuration(repoName, "3.0", publicId = requestedRun)
            createRunWithTargetDuration(repoName, "10.0")

            val response = client.get("/run/${requestedRun.id}/cases/slow/regressions")

            val result = objectMapper.readValue(response.bodyAsText(), SlowTestRegressions::class.java)

            expectThat(result.baselineRunCount).isEqualTo(3)
            expectThat(result.regressions).hasSize(1)
            expectThat(result.regressions[0].baselineDuration).isEqualTo(BigDecimal("1.000"))
        }

    @Test
    fun `should limit baseline to the most recent runs and use the requested threshold`() =
        projektorTestApplication {
            val repoName = "${RandomStringUtils.randomAlphabetic(12)}/repo"

            repeat(3) { createRunWithTargetDuration(repoName, "10.0") }
            repeat(3) { createRunWithTargetDuration(repoName, "1.0") }
            val requestedRun = randomPublicId()
            createRunWithTargetDuration(repoName, "3.0", publicId = requestedRun)

            val recentBaselineResult =
                objectMapper.readValue(
                    client.get("/run/${requestedRun.id}/cases/slow/regressions?baseline_runs=3").bodyAsText(),
                    SlowTestRegressions::class.java,
                )
            expectThat(recentBaselineResult.baselineRunCount).isEqualTo(3)
            expectThat(recentBaselineResult.regressions).hasSize(1)
            expectThat(recentBaselineResult.regressions[0].baselineDuration).isEqualTo(BigDecimal("1.000"))

            val higherThresholdResult =
                objectMapper.readValue(
                    client.get("/run/${requestedRun.id}/cases/slow/regressions?baseline_runs=3&threshold_percent=250").bodyAsText(),
                    SlowTestRegressions::class.java,
                )
            expectThat(higherThresholdResult.thresholdPercent).isEqualTo(250)
            expectThat(higherThresholdResult.regressions).isEmpty()
        }

    @Test
    fun `should return no regressions when test run has no git metadata`() =
        projektorTestApplication {
            val publicId = randomPublicId()
            testRunDBGenerator.createTestRun(publicId, passingSuite)

            val response = client.get("/run/${publicId.id}/cases/slow/regressions")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val result = objectMapper.readValue(response.bodyAsText(), SlowTestRegressions::class.java)

            expectThat(result.baselineRunCount).isEqualTo(0)
            expectThat(result.regressions).isEmpty()
        }

    private fun createRunWithTargetDuration(
        repoName: String,
        targetDuration: String,
        publicId: PublicId = randomPublicId(),
        suite: List<TestSuiteData> = passingSuite,
        ci: Boolean = true,
        branchName: String = "main",
    ): TestRun {
        val testRun = testRunDBGenerator.createTestRunInRepo(publicId, suite, repoName, ci, null, branchName)

        dslContext.update(TEST_CASE)
            .set(TEST_CASE.DURATION, BigDecimal(targetDuration))
            .where(
                TEST_CASE.NAME.eq("target").and(
                    TEST_CASE.TEST_SUITE_ID.`in`(
                        dslContext.select(TEST_SUITE.ID).from(TEST_SUITE).where(TEST_SUITE.TEST_RUN_ID.eq(testRun.id)),
                    ),
                ),
            )
            .execute()

        return testRun
    }
}
