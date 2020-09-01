package projektor.coverage

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import java.math.BigDecimal
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.Coverage
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@KtorExperimentalAPI
class ComparePreviousCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `when coverage went down should compare coverage with previous test run`() {
        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/coverage") {

                val previousTestRun = testRunDBGenerator.createSimpleTestRun(previousPublicId)
                testRunDBGenerator.addGitMetadata(previousTestRun, repoName, true, "main")
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), previousPublicId) }

                val thisPublicTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
                testRunDBGenerator.addGitMetadata(thisPublicTestRun, repoName, true, "main")
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverAppReduced(), thisPublicId) }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverage = objectMapper.readValue(response.content, Coverage::class.java)
                assertNotNull(coverage)

                expectThat(coverage) {
                    get { previousTestRunId }.isEqualTo(previousPublicId.id)
                }

                expectThat(coverage.overallStats) {
                    get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("95.40"))
                    get { lineStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-2.04"))

                    get { statementStat.coveredPercentage }.isEqualTo(BigDecimal("96.12"))
                    get { statementStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-0.22"))

                    get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("68.95"))
                    get { branchStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-8.07"))
                }

                expectThat(coverage.groups).hasSize(1)

                val serverAppGroup = coverage.groups.find { it.name == "server-app" }
                assertNotNull(serverAppGroup)

                expectThat(serverAppGroup.stats) {
                    get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("95.40"))
                    get { lineStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-2.04"))

                    get { statementStat.coveredPercentage }.isEqualTo(BigDecimal("96.12"))
                    get { statementStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-0.22"))

                    get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("68.95"))
                    get { branchStat.coveredPercentageDelta }.isEqualTo(BigDecimal("-8.07"))
                }
            }
        }
    }

    @Test
    fun `when coverage went up should compare coverage with previous test run`() {
        val repoName = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"

        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/coverage") {

                val previousTestRun = testRunDBGenerator.createSimpleTestRun(previousPublicId)
                testRunDBGenerator.addGitMetadata(previousTestRun, repoName, true, "main")
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverAppReduced(), previousPublicId) }

                val thisPublicTestRun = testRunDBGenerator.createSimpleTestRun(thisPublicId)
                testRunDBGenerator.addGitMetadata(thisPublicTestRun, repoName, true, "main")
                runBlocking { coverageService.saveReport(JacocoXmlLoader().serverApp(), thisPublicId) }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val coverage = objectMapper.readValue(response.content, Coverage::class.java)
                assertNotNull(coverage)

                expectThat(coverage) {
                    get { previousTestRunId }.isEqualTo(previousPublicId.id)
                }

                expectThat(coverage.overallStats) {
                    get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                    get { lineStat.coveredPercentageDelta }.isEqualTo(BigDecimal("2.04"))

                    get { statementStat.coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                    get { statementStat.coveredPercentageDelta }.isEqualTo(BigDecimal("0.22"))

                    get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                    get { branchStat.coveredPercentageDelta }.isEqualTo(BigDecimal("8.07"))
                }

                expectThat(coverage.groups).hasSize(1)

                val serverAppGroup = coverage.groups.find { it.name == "server-app" }
                assertNotNull(serverAppGroup)

                expectThat(serverAppGroup.stats) {
                    get { lineStat.coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                    get { lineStat.coveredPercentageDelta }.isEqualTo(BigDecimal("2.04"))

                    get { statementStat.coveredPercentage }.isEqualTo(BigDecimal("96.34"))
                    get { statementStat.coveredPercentageDelta }.isEqualTo(BigDecimal("0.22"))

                    get { branchStat.coveredPercentage }.isEqualTo(BigDecimal("77.02"))
                    get { branchStat.coveredPercentageDelta }.isEqualTo(BigDecimal("8.07"))
                }
            }
        }
    }
}
