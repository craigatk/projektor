package projektor.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import projektor.server.api.TestCase
import projektor.server.api.TestOutput
import projektor.server.api.TestRun
import projektor.server.api.TestRunSummary
import projektor.server.api.TestSuite
import projektor.server.api.ai.TestCaseFailureAnalysis
import projektor.server.api.attachments.Attachments
import projektor.server.api.attributes.TestRunSystemAttributes
import projektor.server.api.config.ServerConfig
import projektor.server.api.coverage.Coverage
import projektor.server.api.coverage.CoverageExists
import projektor.server.api.coverage.CoverageFiles
import projektor.server.api.debug.TestCaseDebugContext
import projektor.server.api.error.ResultsProcessingFailure
import projektor.server.api.failure.FailureClusters
import projektor.server.api.history.TestCaseHistory
import projektor.server.api.messages.Messages
import projektor.server.api.metadata.TestRunGitMetadata
import projektor.server.api.organization.OrganizationCoverage
import projektor.server.api.quality.CodeQualityReports
import projektor.server.api.repository.RepositoryFlakyTests
import projektor.server.api.repository.RepositoryTestRunTimeline
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.performance.RepositoryPerformanceTimeline
import projektor.server.api.results.ResultsProcessing
import strikt.api.expectThat
import strikt.assertions.isEmpty
import java.io.File
import kotlin.reflect.KClass

/**
 * Checks that the UI Cypress tests' stubbed API responses (ui/cypress/fixtures) match what the server actually returns.
 * Otherwise the UI tests keep passing against responses the server no longer sends (or never sent), while the real
 * UI breaks.
 *
 * Each fixture must:
 * - deserialize into its API type, without missing required fields
 * - have exactly the fields the server includes when it serializes that type: none missing, and none extra
 *   (e.g. a field the server used to return). Computed properties the server serializes, like a test case's
 *   full_name, count as fields the server includes.
 */
class UiFixtureContractTest {
    private val fixturesDirectory = File("../../ui/cypress/fixtures")

    // Configured like the server's ContentNegotiation, see Application.kt
    private val serverObjectMapper: ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    private val strictObjectMapper: ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().enable(KotlinFeature.StrictNullChecks).build())
            .registerModule(JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            // Unknown fields are checked by comparing with the server's serialization instead, which allows the
            // computed properties the server includes
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

    private val fixtureTypes: Map<String, JavaType> =
        mapOf(
            "failed_test_case_1.json" to single(TestCase::class),
            "failed_test_case_2.json" to single(TestCase::class),
            "failed_test_case_system_out_err_test_case_level.json" to single(TestCase::class),
            "failed_test_cases.json" to listOf(TestCase::class),
            "failed_test_cases_20.json" to listOf(TestCase::class),
            "failed_test_cases_6.json" to listOf(TestCase::class),
            "slow_test_case_1.json" to single(TestCase::class),
            "slow_test_cases.json" to listOf(TestCase::class),
            "test_case_summaries_in_package.json" to listOf(TestSuite::class),
            "test_output_system_err.json" to single(TestOutput::class),
            "test_output_system_out.json" to single(TestOutput::class),
            "test_run.json" to single(TestRun::class),
            "test_run_summary.json" to single(TestRunSummary::class),
            "test_suite_failing.json" to single(TestSuite::class),
            "test_suite_with_output.json" to single(TestSuite::class),
            "admin/recent_failures_2.json" to listOf(ResultsProcessingFailure::class),
            "ai/failure_cluster_analysis.json" to single(TestCaseFailureAnalysis::class),
            "ai/test_case_debug_context.json" to single(TestCaseDebugContext::class),
            "ai/test_case_failure_analysis.json" to single(TestCaseFailureAnalysis::class),
            "admin/recent_failures_20.json" to listOf(ResultsProcessingFailure::class),
            "attachments/attachments.json" to single(Attachments::class),
            "attachments/attachments_empty.json" to single(Attachments::class),
            "attachments/cypress-screenshot-video-attachments.json" to single(Attachments::class),
            "attachments/test_run_summary_with_attachments.json" to single(TestRunSummary::class),
            "attributes/not_pinned.json" to single(TestRunSystemAttributes::class),
            "attributes/pinned.json" to single(TestRunSystemAttributes::class),
            "config/ai_config_enabled.json" to single(ServerConfig::class),
            "config/cleanup_config_disabled.json" to single(ServerConfig::class),
            "config/cleanup_config_enabled.json" to single(ServerConfig::class),
            "config/server_config_disabled.json" to single(ServerConfig::class),
            "config/slack_config_enabled.json" to single(ServerConfig::class),
            "coverage/coverage-does-not-exist.json" to single(CoverageExists::class),
            "coverage/coverage-exists.json" to single(CoverageExists::class),
            "coverage/coverage-files-server-app.json" to single(CoverageFiles::class),
            "coverage/coverage-files-with-file-path.json" to single(CoverageFiles::class),
            "coverage/coverage-three-groups-2.json" to single(Coverage::class),
            "coverage/coverage-three-groups-previous-run.json" to single(Coverage::class),
            "coverage/coverage-three-groups.json" to single(Coverage::class),
            "cypress/failed_test_case_with_screenshot_and_video.json" to single(TestCase::class),
            "cypress/failed_test_cases_with_screenshot_and_video.json" to listOf(TestCase::class),
            "cypress/test_case_side_nav.json" to single(TestCase::class),
            "cypress/test_run.json" to single(TestRun::class),
            "cypress/test_run_summary.json" to single(TestRunSummary::class),
            "cypress/test_suite_side_nav.json" to single(TestSuite::class),
            "failure_clusters/failure_clusters.json" to single(FailureClusters::class),
            "failure_clusters/failure_clusters_unrelated.json" to single(FailureClusters::class),
            "messages/no_messages.json" to single(Messages::class),
            "messages/one_message.json" to single(Messages::class),
            "metadata/git-metadata-with-github-base-url.json" to single(TestRunGitMetadata::class),
            "metadata/git-metadata-with-pr-and-commit.json" to single(TestRunGitMetadata::class),
            "one_passing/test_case.json" to single(TestCase::class),
            "one_passing/test_run.json" to single(TestRun::class),
            "one_passing/test_run_summary.json" to single(TestRunSummary::class),
            "one_passing/test_suite.json" to single(TestSuite::class),
            "organization/organization_coverage.json" to single(OrganizationCoverage::class),
            "processing_status/processing_status_deleted.json" to single(ResultsProcessing::class),
            "processing_status/processing_status_error.json" to single(ResultsProcessing::class),
            "processing_status/processing_status_processing.json" to single(ResultsProcessing::class),
            "processing_status/processing_status_success.json" to single(ResultsProcessing::class),
            "quality/code_quality_reports.json" to single(CodeQualityReports::class),
            "quality/empty_code_quality_reports.json" to single(CodeQualityReports::class),
            "repository/coverage_exists_true.json" to single(CoverageExists::class),
            "repository/coverage_timeline.json" to single(RepositoryCoverageTimeline::class),
            "repository/coverage_timeline_empty.json" to single(RepositoryCoverageTimeline::class),
            "repository/flaky_tests.json" to single(RepositoryFlakyTests::class),
            "repository/flaky_tests_two_tests.json" to single(RepositoryFlakyTests::class),
            "repository/performance_timeline.json" to single(RepositoryPerformanceTimeline::class),
            "repository/timeline.json" to single(RepositoryTestRunTimeline::class),
            "test_case_history/empty_history.json" to single(TestCaseHistory::class),
            "test_case_history/failing_history.json" to single(TestCaseHistory::class),
        )

    @Test
    fun `every JSON fixture should have an API type to check it against`() {
        val jsonFixtures =
            fixturesDirectory.walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .map { it.relativeTo(fixturesDirectory).invariantSeparatorsPath }
                .toList()

        expectThat(jsonFixtures.filter { it !in fixtureTypes.keys }.sorted()).isEmpty()
    }

    @TestFactory
    fun `fixture should match the server's API response`(): List<DynamicTest> =
        fixtureTypes.map { (fixturePath, type) ->
            DynamicTest.dynamicTest(fixturePath) {
                val fixtureJson = serverObjectMapper.readTree(File(fixturesDirectory, fixturePath))

                val value =
                    try {
                        strictObjectMapper.readValue<Any>(strictObjectMapper.treeAsTokens(fixtureJson), type)
                    } catch (e: Exception) {
                        throw AssertionError("$fixturePath doesn't match ${type.toCanonical()}: ${e.message?.lineSequence()?.first()}", e)
                    }

                val serverJson = serverObjectMapper.valueToTree<JsonNode>(value)

                val missingFields = missingFields(expected = serverJson, actual = fixtureJson, path = "")
                val extraFields = missingFields(expected = fixtureJson, actual = serverJson, path = "")
                if (missingFields.isNotEmpty() || extraFields.isNotEmpty()) {
                    throw AssertionError(
                        "$fixturePath doesn't match the server's response. Missing fields: $missingFields, fields the server doesn't return: $extraFields",
                    )
                }
            }
        }

    private fun missingFields(
        expected: JsonNode,
        actual: JsonNode,
        path: String,
    ): List<String> =
        when {
            expected.isObject && actual.isObject ->
                expected.fieldNames().asSequence().flatMap { field ->
                    val fieldPath = if (path.isEmpty()) field else "$path.$field"
                    if (!actual.has(field)) {
                        sequenceOf(fieldPath)
                    } else {
                        missingFields(expected.get(field), actual.get(field), fieldPath).asSequence()
                    }
                }.toList()
            expected.isArray && actual.isArray ->
                (0 until minOf(expected.size(), actual.size())).flatMap { idx ->
                    missingFields(expected.get(idx), actual.get(idx), "$path[$idx]")
                }.distinctBy { it.replace(Regex("\\[\\d+]"), "[]") }
            else -> listOf()
        }

    private fun single(type: KClass<*>): JavaType = serverObjectMapper.typeFactory.constructType(type.java)

    private fun listOf(type: KClass<*>): JavaType = serverObjectMapper.typeFactory.constructCollectionType(List::class.java, type.java)
}
