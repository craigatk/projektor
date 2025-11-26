package projektor.parser.coverage

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.coverage.CloverXmlLoader
import projektor.server.example.coverage.CoberturaXmlLoader
import projektor.server.example.coverage.GoCoverageLoader
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.math.BigDecimal
import kotlin.test.assertNotNull

class CoverageParserSpec : StringSpec({
    "should parse Jacoco coverage report" {
        val jacocoReportXml = JacocoXmlLoader().serverApp()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml, null)

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

    "should parse Clover coverage report" {
        val cloverReportXml = CloverXmlLoader().uiClover()

        val coverageReport = CoverageParser.parseReport(cloverReportXml, null)

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

    "should parse Cobertura coverage report" {
        val coberturaReportXml = CoberturaXmlLoader().nodeScriptCobertura()

        val coverageReport = CoverageParser.parseReport(coberturaReportXml, null)
        assertNotNull(coverageReport)

        expectThat(coverageReport.totalStats.lineStat) {
            get { covered }.isEqualTo(231)
            get { total }.isEqualTo(234)
            get { percentCovered }.isEqualTo(BigDecimal("98.72"))
        }

        expectThat(coverageReport.totalStats.branchStat) {
            get { covered }.isEqualTo(120)
            get { total }.isEqualTo(128)
            get { percentCovered }.isEqualTo(BigDecimal("93.75"))
        }
    }

    "should parse Cobertura report without Doctype" {
        val coberturaReportXml = CoberturaXmlLoader().noDoctypeCobertura()

        val coverageReport = CoverageParser.parseReport(coberturaReportXml, null)
        assertNotNull(coverageReport)

        expectThat(coverageReport.totalStats.lineStat) {
            get { covered }.isEqualTo(231)
            get { total }.isEqualTo(234)
            get { percentCovered }.isEqualTo(BigDecimal("98.72"))
        }
    }

    "when report does not have any branch coverage should parse it" {
        val reportXmlWithoutBranchCoverage = JacocoXmlLoader().junitResultsParser()

        val coverageReport = CoverageParser.parseReport(reportXmlWithoutBranchCoverage, null)

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
        expectThat(CoverageParser.parseReport("unknown", null)).isNull()
    }

    "should parse Go coverage report" {
        val goCoverageReport = GoCoverageLoader().simpleCoverage()

        val coverageReport = CoverageParser.parseReport(goCoverageReport, null)

        expectThat(coverageReport).isNotNull().and {
            get { name }.isEqualTo("Coverage")

            get { totalStats.lineStat.covered }.isEqualTo(6)
            get { totalStats.lineStat.missed }.isEqualTo(6)
            get { totalStats.lineStat.total }.isEqualTo(12)
            get { totalStats.lineStat.percentCovered }.isEqualTo(BigDecimal("50.00"))

            get { totalStats.statementStat.covered }.isEqualTo(2)
            get { totalStats.statementStat.missed }.isEqualTo(2)
            get { totalStats.statementStat.total }.isEqualTo(4)
            get { totalStats.statementStat.percentCovered }.isEqualTo(BigDecimal("50.00"))

            get { totalStats.branchStat.covered }.isEqualTo(0)
            get { totalStats.branchStat.missed }.isEqualTo(0)
            get { totalStats.branchStat.total }.isEqualTo(0)
        }
    }

    "should parse Go multi-file coverage report" {
        val goCoverageReport = GoCoverageLoader().multiFileCoverage()

        val coverageReport = CoverageParser.parseReport(goCoverageReport, null)

        expectThat(coverageReport).isNotNull().and {
            get { name }.isEqualTo("Coverage")
            get { files?.size }.isEqualTo(3)
        }

        val calculatorFile = coverageReport?.files?.find { it.fileName == "calculator.go" }
        expectThat(calculatorFile).isNotNull().and {
            get { directoryName }.isEqualTo("github.com/user/project/pkg/calculator")
            get { filePath }.isEqualTo("github.com/user/project/pkg/calculator/calculator.go")
        }
    }
})
