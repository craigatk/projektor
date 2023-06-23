package projektor.coverage

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.jooq.exception.DataAccessException
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.compare.PreviousTestRunService
import projektor.error.ProcessingFailureService
import projektor.metrics.MetricsService
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.PublicId
import projektor.server.example.coverage.CloverXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class CoverageServiceTest : DatabaseRepositoryTestCase() {
    @Test
    fun `when saving coverage fails should record metric`() {
        val coverageRepository: CoverageRepository by inject()
        val meterRegistry = SimpleMeterRegistry()
        val metricsService = MetricsService(meterRegistry)
        val previousTestRunService: PreviousTestRunService by inject()
        val processingFailureService: ProcessingFailureService by inject()
        val coverageService = CoverageService(
            coverageRepository,
            metricsService,
            previousTestRunService,
            processingFailureService
        )

        val coverageFilePayload = CoverageFilePayload(reportContents = JacocoXmlLoader().junitResultsParser())
        val publicId = PublicId("this-is-too-long-for-an-id")

        expectCatching { coverageService.saveReport(coverageFilePayload, publicId) }
            .isFailure()
            .isA<DataAccessException>()

        expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(1.toDouble())
        expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())
    }

    @Test
    fun `should parse coverage file into pair of group and lines`() {
        val coverageRepository: CoverageRepository by inject()
        val meterRegistry = SimpleMeterRegistry()
        val metricsService = MetricsService(meterRegistry)
        val previousTestRunService: PreviousTestRunService by inject()
        val processingFailureService: ProcessingFailureService by inject()
        val coverageService = CoverageService(
            coverageRepository,
            metricsService,
            previousTestRunService,
            processingFailureService
        )

        val reportContents = CloverXmlLoader().coverageFilesTable()
        val coverageFilePayload = CoverageFilePayload(
            reportContents = reportContents
        )

        val parsedReportPair = runBlocking { coverageService.parseReport(coverageFilePayload) }
        assertNotNull(parsedReportPair)
        val (coverageGroup, coverageFiles) = parsedReportPair

        expectThat(coverageGroup) {
            get { name }.isEqualTo("All files")
        }

        expectThat(coverageFiles).hasSize(7)
        expectThat(coverageFiles.find { it.fileName == "CoverageFilesTable.tsx" }).isNotNull().and {
            get { stats.lineStat.total }.isEqualTo(30)
            get { stats.lineStat.covered }.isEqualTo(27)
            get { stats.lineStat.missed }.isEqualTo(3)
            get { missedLines.toList() }.hasSize(3).contains(124, 149, 172)
        }
        expectThat(coverageFiles.find { it.fileName == "VersionControlHelpers.ts" }).isNotNull().and {
            get { stats.lineStat.total }.isEqualTo(8)
            get { stats.lineStat.covered }.isEqualTo(5)
            get { stats.lineStat.missed }.isEqualTo(3)
            get { missedLines.toList() }.hasSize(3).contains(10, 14, 16)
        }
    }
}
