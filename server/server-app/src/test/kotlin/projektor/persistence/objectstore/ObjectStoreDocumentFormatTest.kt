package projektor.persistence.objectstore

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import projektor.notification.slack.RepositorySlackChannelObjectStoreRepository.SlackChannelDocument
import projektor.objectstore.json.JsonObjectStore
import projektor.repository.coverage.RepositoryCoverageObjectStoreRepository.CurrentCoverageDocument
import projektor.testrun.TestRunObjectStoreRepository.GroupClaimDocument
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull

/**
 * Pins the stored JSON format of every object-store document. Documents already in a bucket have to stay readable,
 * so a renamed or retyped property must fail here rather than silently breaking existing data. Each fixture is read
 * and written back, so the format is pinned in both directions.
 *
 * Adding a property: give it a default so older documents (without it) still read, update its fixture,
 * and add a legacy fixture without it.
 */
class ObjectStoreDocumentFormatTest {
    private val objectMapper = JsonObjectStore.createObjectMapper()

    @TestFactory
    fun `stored documents should read and write back unchanged`() =
        currentDocuments.map { (fixtureName, documentType) ->
            DynamicTest.dynamicTest(fixtureName) {
                val fixture = readFixture(fixtureName)

                val document = objectMapper.readValue(fixture, documentType)
                val rewritten = objectMapper.writeValueAsString(document)

                expectThat(objectMapper.readTree(rewritten)).isEqualTo(objectMapper.readTree(fixture))
            }
        }

    @Test
    fun `test run written before the deleted flag should read as not deleted`() {
        val testRun = objectMapper.readValue(readFixture("legacy-test-run-without-deleted"), TestRunDocument::class.java)

        expectThat(testRun.deleted).isFalse()
        expectThat(testRun.testSuites.map { it.className }).isEqualTo(listOf("SuiteTest"))
    }

    @Test
    fun `coverage group written before append tracking should read as not created by append`() {
        val coverageRun = objectMapper.readValue(readFixture("legacy-coverage-run-without-append-fields"), CoverageRunDocument::class.java)

        expectThat(coverageRun.groups.single().createdByAppend).isFalse()
        expectThat(coverageRun.groups.single().appendCount).isEqualTo(0)
    }

    @Test
    fun `group claim written before claim timestamps should read without one`() {
        val claim = objectMapper.readValue(readFixture("legacy-group-claim-without-claimed-at"), GroupClaimDocument::class.java)

        expectThat(claim.testRunPublicId).isEqualTo("ABC123")
        expectThat(claim.claimedAt).isNull()
    }

    @Test
    fun `index repair marker without the saving flag should read as written after a failure`() {
        val marker = objectMapper.readValue(readFixture("legacy-index-repair-without-saving-test-run"), IndexRepairDocument::class.java)

        expectThat(marker.savingTestRun).isFalse()
    }

    private fun readFixture(name: String): String =
        javaClass.getResource("/objectstore-documents/$name.json")?.readText()
            ?: throw IllegalArgumentException("Missing fixture objectstore-documents/$name.json")

    companion object {
        private val currentDocuments: List<Pair<String, Class<*>>> =
            listOf(
                Pair("test-run", TestRunDocument::class.java),
                Pair("test-case-chunk", TestCaseChunkDocument::class.java),
                Pair("output", OutputDocument::class.java),
                Pair("system-attributes", SystemAttributesDocument::class.java),
                Pair("performance-results", PerformanceResultsDocument::class.java),
                Pair("code-quality-reports", CodeQualityReportsDocument::class.java),
                Pair("attachments", AttachmentsDocument::class.java),
                Pair("results-processing", ResultsProcessingDocument::class.java),
                Pair("processing-failure", ProcessingFailureDocument::class.java),
                Pair("coverage-run", CoverageRunDocument::class.java),
                Pair("coverage-files", CoverageFilesDocument::class.java),
                Pair("timeline-shard", TimelineShardDocument::class.java),
                Pair("created-marker", CreatedMarkerDocument::class.java),
                Pair("organization-repository", OrganizationRepositoryDocument::class.java),
                Pair("index-repair", IndexRepairDocument::class.java),
                Pair("lock-lease", LockLeaseDocument::class.java),
                Pair("group-claim", GroupClaimDocument::class.java),
                Pair("current-coverage", CurrentCoverageDocument::class.java),
                Pair("slack-channel", SlackChannelDocument::class.java),
            )
    }
}
