package projektor.parser.coverage

import io.kotest.core.spec.style.StringSpec
import java.math.BigDecimal
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JestXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class CoverageParserSpec : StringSpec({
    "should parse Jacoco test report" {
        val jacocoReportXml = JacocoXmlLoader().serverApp()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml)

        expectThat(coverageReport).isNotNull().and {
            get { name }.isEqualTo("server-app")

            get { totalStats.lineStat.covered }.isEqualTo(953)
            get { totalStats.lineStat.missed }.isEqualTo(25)
            get { totalStats.lineStat.total }.isEqualTo(978)
            get { totalStats.lineStat.percentCovered }.isEqualTo(BigDecimal("97.44"))

            get { totalStats.statementStat.covered }.isEqualTo(8816)
            get { totalStats.statementStat.missed }.isEqualTo(335)
            get { totalStats.statementStat.total }.isEqualTo(9151)
            get { totalStats.statementStat.percentCovered }.isEqualTo(BigDecimal("96.34"))

            get { totalStats.branchStat.covered }.isEqualTo(191)
            get { totalStats.branchStat.missed }.isEqualTo(57)
            get { totalStats.branchStat.total }.isEqualTo(248)
            get { totalStats.branchStat.percentCovered }.isEqualTo(BigDecimal("77.02"))
        }
    }

    "should parse Jest test report" {
        val jestReportXml = JestXmlLoader().ui()

        val coverageReport = CoverageParser.parseReport(jestReportXml)

        expectThat(coverageReport).isNotNull().and {
            get { name }.isEqualTo("All files")

            get { totalStats.lineStat.covered }.isEqualTo(924)
            get { totalStats.lineStat.missed }.isEqualTo(97)
            get { totalStats.lineStat.total }.isEqualTo(1021)
            get { totalStats.lineStat.percentCovered }.isEqualTo(BigDecimal("90.50"))

            get { totalStats.branchStat.covered }.isEqualTo(158)
            get { totalStats.branchStat.missed }.isEqualTo(37)
            get { totalStats.branchStat.total }.isEqualTo(195)
            get { totalStats.branchStat.percentCovered }.isEqualTo(BigDecimal("81.03"))

            get { totalStats.statementStat.covered }.isEqualTo(0)
            get { totalStats.statementStat.missed }.isEqualTo(0)
            get { totalStats.statementStat.total }.isEqualTo(0)
        }
    }

    "when report does not have any branch coverage should parse it" {
        val reportXmlWithoutBranchCoverage = JacocoXmlLoader().junitResultsParser()

        val coverageReport = CoverageParser.parseReport(reportXmlWithoutBranchCoverage)

        expectThat(coverageReport).isNotNull().and {
            get { name }.isEqualTo("junit-results-parser")

            get { totalStats.lineStat.covered }.isEqualTo(12)
            get { totalStats.lineStat.missed }.isEqualTo(1)
            get { totalStats.lineStat.total }.isEqualTo(13)
            get { totalStats.lineStat.percentCovered }.isEqualTo(BigDecimal("92.31"))

            get { totalStats.statementStat.covered }.isEqualTo(46)
            get { totalStats.statementStat.missed }.isEqualTo(12)
            get { totalStats.statementStat.total }.isEqualTo(58)
            get { totalStats.statementStat.percentCovered }.isEqualTo(BigDecimal("79.31"))

            get { totalStats.branchStat.covered }.isEqualTo(0)
            get { totalStats.branchStat.missed }.isEqualTo(0)
            get { totalStats.branchStat.total }.isEqualTo(0)
            get { totalStats.branchStat.percentCovered }.isEqualTo(BigDecimal.ZERO)
        }
    }

    "when unknown report type should return null" {
        expectThat(CoverageParser.parseReport("unknown")).isNull()
    }
})
