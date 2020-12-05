package projektor.parser.coverage

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JestXmlLoader
import strikt.api.expectThat
import strikt.assertions.*
import java.math.BigDecimal
import kotlin.test.assertNotNull

class CoverageParserFileSpec : StringSpec({
    "should parse file-level stats from Jacoco test report" {
        val jacocoReportXml = JacocoXmlLoader().serverApp()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml, null)
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files
        assertNotNull(coverageReportFiles)

        expectThat(coverageReportFiles).hasSize(62)

        val cleanupScheduledJobFile = coverageReportFiles.find { it.fileName == "CleanupScheduledJob.kt" }
        expectThat(cleanupScheduledJobFile).isNotNull().and {
            get { directoryName }.isEqualTo("projektor/cleanup")
            get { missedLines }.hasSize(4).contains(16, 18, 20, 21)
            get { partialLines }.hasSize(0)
            get { stats }.and {
                get { lineStat }.get { percentCovered }.isEqualTo(BigDecimal("71.43"))
                get { branchStat }.get { percentCovered }.isEqualTo(BigDecimal("100.00"))
            }
        }

        val gzipUtilFile = coverageReportFiles.find { it.fileName == "GzipUtil.kt" }
        expectThat(gzipUtilFile).isNotNull().and {
            get { directoryName }.isEqualTo("projektor/util")
            get { missedLines }.hasSize(0)
            get { partialLines }.hasSize(2).contains(9, 14)
            get { stats }.and {
                get { lineStat }.get { percentCovered }.isEqualTo(BigDecimal("100.00"))
                get { branchStat }.get { percentCovered }.isEqualTo(BigDecimal("50.00"))
            }
        }
    }

    "should include full file path for Jacoco results when base directory set"() {
        val jacocoReportXml = JacocoXmlLoader().serverApp()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml, "server/server-app/src/main/kotlin")
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files

        val cleanupScheduledJobFile = coverageReportFiles?.find { it.fileName == "CleanupScheduledJob.kt" }
        expectThat(cleanupScheduledJobFile).isNotNull().and {
            get { directoryName }.isEqualTo("projektor/cleanup")
            get { filePath }.isNotNull().isEqualTo("server/server-app/src/main/kotlin/projektor/cleanup/CleanupScheduledJob.kt")
        }
    }

    "should parse file-level stats from Jest Clover coverage report" {
        val reportXml = JestXmlLoader().uiClover2()

        val coverageReport = CoverageParser.parseReport(reportXml, null)
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files
        assertNotNull(coverageReportFiles)

        val coverageSummaryFile = coverageReportFiles.find { it.fileName == "CoverageSummary.tsx" }

        expectThat(coverageSummaryFile).isNotNull().and {
            get { directoryName }.isEqualTo("src/Coverage")
            get { filePath }.isEqualTo("src/Coverage/CoverageSummary.tsx")
            get { missedLines }.hasSize(3).contains(19, 20, 26)
            get { partialLines }.hasSize(0)
            get { stats.lineStat.percentCovered }.isEqualTo(BigDecimal("82.35"))
            get { stats.branchStat.percentCovered }.isEqualTo(BigDecimal("50.00"))
        }
    }

    "should set file path in Clover results when base directory path set" {
        val reportXml = JestXmlLoader().uiClover2()

        val coverageReport = CoverageParser.parseReport(reportXml, "ui")
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files
        assertNotNull(coverageReportFiles)

        val coverageSummaryFile = coverageReportFiles.find { it.fileName == "CoverageSummary.tsx" }
        expectThat(coverageSummaryFile).isNotNull().and {
            get { directoryName }.isEqualTo("src/Coverage")
            get { filePath }.isEqualTo("ui/src/Coverage/CoverageSummary.tsx")
        }
    }

    "should not show any partial lines in Clover stats because they aren't accurate" {
        val jacocoReportXml = JestXmlLoader().uiClover2()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml, null)
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files
        assertNotNull(coverageReportFiles)

        expectThat(coverageReportFiles).all {
            get { partialLines }.hasSize(0)
        }
    }
})
