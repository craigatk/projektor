package projektor.parser.coverage

import io.kotest.core.spec.style.StringSpec
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.*
import java.math.BigDecimal
import kotlin.test.assertNotNull

class CoverageParserFileSpec : StringSpec({
    "should parse file-level stats from Jacoco test report" {
        val jacocoReportXml = JacocoXmlLoader().serverApp()

        val coverageReport = CoverageParser.parseReport(jacocoReportXml)
        assertNotNull(coverageReport)

        val coverageReportFiles = coverageReport.files
        assertNotNull(coverageReportFiles)

        expectThat(coverageReportFiles).hasSize(62)

        val cleanupScheduledJobFile = coverageReportFiles.find { it.fileName == "CleanupScheduledJob.kt" }
        expectThat(cleanupScheduledJobFile).isNotNull().and {
            get { missedLines }.hasSize(4).contains(16, 18, 20, 21)
            get { partialLines }.hasSize(0)
            get { stats }.and {
                get { lineStat }.get { percentCovered }.isEqualTo(BigDecimal("71.43"))
                get { branchStat }.get { percentCovered }.isEqualTo(BigDecimal("100.00"))
            }
        }

        val gzipUtilFile = coverageReportFiles.find { it.fileName == "GzipUtil.kt" }
        expectThat(gzipUtilFile).isNotNull().and {
            get { missedLines }.hasSize(0)
            get { partialLines }.hasSize(2).contains(9, 14)
            get { stats }.and {
                get { lineStat }.get { percentCovered }.isEqualTo(BigDecimal("100.00"))
                get { branchStat }.get { percentCovered }.isEqualTo(BigDecimal("50.00"))
            }
        }
    }
})
