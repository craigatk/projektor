package projektor.persistence.objectstore

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/*
 * Documents stored by the object-store persistence backend. These are kept separate from the API types
 * so the stored format is stable and versioned independently of the API.
 *
 * Values are normalized the same way the Postgres schema stores them (numeric(12, 3) durations,
 * microsecond timestamps) so both backends return equal results.
 */

const val DOCUMENT_SCHEMA_VERSION = 1

data class TestRunDocument(
    val schemaVersion: Int = DOCUMENT_SCHEMA_VERSION,
    val summary: TestRunSummaryDocument,
    val resultsMetadata: ResultsMetadataDocument?,
    val gitMetadata: GitMetadataDocument?,
    val testSuiteGroups: List<TestSuiteGroupDocument>,
    // Test suites without their test cases, which are stored in separate chunk documents
    val testSuites: List<TestSuiteDocument>,
    // Appends reserve their test suite indexes before writing so concurrent appends don't collide
    val lastReservedTestSuiteIdx: Int,
    // Set when deleting the test run starts, so it reads as deleted (and leaves the indexes) before any of its
    // documents are removed. A delete that fails partway can then be retried, since this document goes last.
    val deleted: Boolean = false,
)

data class TestRunSummaryDocument(
    val totalTestCount: Int,
    val totalPassingCount: Int,
    val totalSkippedCount: Int,
    val totalFailureCount: Int,
    val passed: Boolean,
    val cumulativeDuration: BigDecimal,
    val averageDuration: BigDecimal,
    val slowestTestCaseDuration: BigDecimal,
    val createdTimestamp: Instant,
    val wallClockDuration: BigDecimal?,
)

data class ResultsMetadataDocument(val ci: Boolean?, val group: String?)

data class GitMetadataDocument(
    val repoName: String,
    val orgName: String?,
    val projectName: String?,
    val branchName: String?,
    val isMainBranch: Boolean,
    val pullRequestNumber: Int?,
    val commitSha: String?,
)

data class TestSuiteGroupDocument(val groupName: String?, val groupLabel: String?, val directory: String?)

data class TestSuiteDocument(
    val idx: Int,
    val fileName: String?,
    val packageName: String?,
    val className: String,
    val testCount: Int,
    val passingCount: Int,
    val skippedCount: Int,
    val failureCount: Int,
    val startTs: LocalDateTime?,
    val hostname: String?,
    val duration: BigDecimal?,
    val hasSystemOut: Boolean,
    val hasSystemErr: Boolean,
    val groupName: String?,
    val groupLabel: String?,
    // Index of the first test suite in the chunk document holding this suite's test cases
    val testCaseChunk: Int,
)

data class TestCaseChunkDocument(val testSuites: List<TestSuiteTestCasesDocument>)

data class TestSuiteTestCasesDocument(val testSuiteIdx: Int, val testCases: List<TestCaseDocument>)

data class TestCaseDocument(
    val idx: Int,
    val name: String,
    val packageName: String?,
    val className: String?,
    val duration: BigDecimal?,
    val passed: Boolean,
    val skipped: Boolean,
    val hasSystemOut: Boolean,
    val hasSystemErr: Boolean,
    val failure: TestFailureDocument?,
)

data class TestFailureDocument(val failureMessage: String?, val failureType: String?, val failureText: String?)

data class OutputDocument(val value: String?)

data class SystemAttributesDocument(val pinned: Boolean)

data class PerformanceResultsDocument(val results: List<PerformanceResultDocument>)

data class PerformanceResultDocument(
    val name: String,
    val requestsPerSecond: BigDecimal,
    val requestCount: Long,
    val average: BigDecimal,
    val maximum: BigDecimal,
    val p95: BigDecimal,
)

data class CodeQualityReportsDocument(val reports: List<CodeQualityReportDocument>)

data class CodeQualityReportDocument(val idx: Int, val contents: String, val fileName: String, val groupName: String?)

data class AttachmentsDocument(val attachments: List<AttachmentDocument>)

data class AttachmentDocument(val fileName: String, val objectName: String, val fileSize: Long?)

data class ResultsProcessingDocument(
    val status: String,
    val createdTimestamp: LocalDateTime?,
    val errorMessage: String?,
)

data class ProcessingFailureDocument(
    val publicId: String,
    val body: String,
    val bodyType: String,
    val createdTimestamp: LocalDateTime,
    val failureMessage: String?,
)

data class CoverageRunDocument(val groups: List<CoverageGroupDocument>)

data class CoverageGroupDocument(
    // Identifies the group's files document, since group names aren't guaranteed to be unique or key-safe
    val id: String,
    val name: String,
    val stats: CoverageStatsDocument,
    // Whether the group was created by appending coverage, and how many appends it's had. A group created by
    // a single append keeps the report's stats, otherwise its line stats are calculated from its files.
    val createdByAppend: Boolean = false,
    val appendCount: Int = 0,
)

data class CoverageStatsDocument(
    val statementCovered: Int,
    val statementMissed: Int,
    val lineCovered: Int,
    val lineMissed: Int,
    val branchCovered: Int,
    val branchMissed: Int,
)

data class CoverageFilesDocument(val files: List<CoverageFileDocument>)

data class CoverageFileDocument(
    val fileName: String,
    val directoryName: String,
    val filePath: String?,
    val missedLines: List<Int>,
    val partialLines: List<Int>,
    val stats: CoverageStatsDocument,
)

/** Matches the numeric(12, 3) columns the database stores durations and performance values in. */
fun BigDecimal.toStoredScale(): BigDecimal = setScale(3, RoundingMode.HALF_UP)

/** Matches the microsecond precision of the database's timestamp columns, which round rather than truncate. */
fun Instant.toStoredPrecision(): Instant = plusNanos(500).truncatedTo(ChronoUnit.MICROS)

fun LocalDateTime.toStoredPrecision(): LocalDateTime = plusNanos(500).truncatedTo(ChronoUnit.MICROS)
