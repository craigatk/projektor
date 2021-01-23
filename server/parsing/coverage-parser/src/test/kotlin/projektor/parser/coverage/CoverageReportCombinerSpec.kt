package projektor.parser.coverage

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class CoverageReportCombinerSpec : StringSpec({
    "should combine coverage data from two reports with the same name" {
        val integrationTestReport = JacocoXmlLoader().combinedIntegrationTest()
        val unitTestReport = JacocoXmlLoader().combinedUnitTest()

        val coverageReports = listOf(integrationTestReport, unitTestReport)
                .mapNotNull { CoverageParser.parseReport(it, "src/main/groovy") }

        val combinedCoverageReports = CoverageReportCombiner.combineCoverageReports(coverageReports)

        expectThat(combinedCoverageReports).hasSize(1)

        val combinedCoverageReport = combinedCoverageReports[0]

        expectThat(combinedCoverageReport.name).isEqualTo("gradle-jacoco-merge")

        expectThat(combinedCoverageReport.totalStats.lineStat) {
            get { covered }.isEqualTo(7)
            get { missed }.isEqualTo(2)
            get { total }.isEqualTo(9)
        }

        expectThat(combinedCoverageReport.totalStats.branchStat) {
            get { covered }.isEqualTo(4)
            get { missed }.isEqualTo(2)
            get { total }.isEqualTo(6)
        }

        expectThat(combinedCoverageReport.totalStats.statementStat) {
            get { covered }.isEqualTo(20)
            get { missed }.isEqualTo(10)
            get { total }.isEqualTo(30)
        }
    }
})
