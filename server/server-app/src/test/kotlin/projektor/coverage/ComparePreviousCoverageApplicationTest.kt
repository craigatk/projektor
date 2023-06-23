package projektor.coverage

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.coverage.Coverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class ComparePreviousCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `when coverage went down should compare coverage with previous test run`() {
        val repoName = randomOrgAndRepo()

        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/coverage") {
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = previousPublicId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = thisPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName
                )
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
        val repoName = randomOrgAndRepo()

        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$thisPublicId/coverage") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = previousPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repoName
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = thisPublicId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName
                )
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
