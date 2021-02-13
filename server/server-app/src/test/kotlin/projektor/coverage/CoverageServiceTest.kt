package projektor.coverage

import io.ktor.util.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.compare.PreviousTestRunService
import projektor.error.ProcessingFailureService
import projektor.metrics.MetricsService
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.PublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure

@KtorExperimentalAPI
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
        val publicId = PublicId("this-is-too-long-for-anid")

        expectCatching {
            runBlocking { coverageService.saveReport(coverageFilePayload, publicId) }
        }.isFailure()

        expectThat(meterRegistry.counter("coverage_process_failure").count()).isEqualTo(1.toDouble())
        expectThat(meterRegistry.counter("coverage_parse_failure").count()).isEqualTo(0.toDouble())
    }
}
