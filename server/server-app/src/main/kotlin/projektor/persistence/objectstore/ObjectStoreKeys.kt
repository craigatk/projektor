package projektor.persistence.objectstore

import projektor.server.api.PublicId
import java.time.Instant

/**
 * Key layout for the object-store persistence backend. Everything a test run owns lives under
 * runs/{publicId}/ so deleting a test run is a single prefix delete. Coverage, attachments and
 * results processing status are deleted separately (see TestRunRepository.deleteTestRun), so they
 * live under their own prefixes.
 */
object ObjectStoreKeys {
    fun testRunPrefix(publicId: PublicId) = "runs/${publicId.id}/"

    fun testRun(publicId: PublicId) = "${testRunPrefix(publicId)}run.json"

    fun testCaseChunk(
        publicId: PublicId,
        firstTestSuiteIdx: Int,
    ) = "${testRunPrefix(publicId)}cases/${firstTestSuiteIdx.toString().padStart(6, '0')}.json"

    fun testSuiteOutput(
        publicId: PublicId,
        testSuiteIdx: Int,
        outputType: OutputType,
    ) = "${testRunPrefix(publicId)}output/suite-$testSuiteIdx-${outputType.keyName}.json"

    fun testCaseOutput(
        publicId: PublicId,
        testSuiteIdx: Int,
        testCaseIdx: Int,
        outputType: OutputType,
    ) = "${testRunPrefix(publicId)}output/case-$testSuiteIdx-$testCaseIdx-${outputType.keyName}.json"

    fun systemAttributes(publicId: PublicId) = "${testRunPrefix(publicId)}attributes.json"

    fun performanceResults(publicId: PublicId) = "${testRunPrefix(publicId)}performance.json"

    fun codeQualityReports(publicId: PublicId) = "${testRunPrefix(publicId)}quality.json"

    fun coveragePrefix(publicId: PublicId) = "coverage/${publicId.id}/"

    fun coverageRun(publicId: PublicId) = "${coveragePrefix(publicId)}coverage.json"

    fun coverageGroupFiles(
        publicId: PublicId,
        groupId: String,
    ) = "${coveragePrefix(publicId)}groups/$groupId.json"

    fun attachments(publicId: PublicId) = "attachments/${publicId.id}.json"

    fun resultsProcessing(publicId: PublicId) = "processing/${publicId.id}.json"

    const val PROCESSING_FAILURES_PREFIX = "failures/"

    /**
     * Newest failures sort first when listing, since S3 lists keys in lexicographic order.
     * The unique suffix keeps multiple failures for the same test run at the same instant apart.
     */
    fun processingFailure(
        createdTimestamp: Instant,
        publicId: PublicId,
        uniqueSuffix: String,
    ) = "$PROCESSING_FAILURES_PREFIX${invertedTimestamp(createdTimestamp)}-${publicId.id}-$uniqueSuffix.json"

    internal fun invertedTimestamp(timestamp: Instant) = (Long.MAX_VALUE - timestamp.toEpochMilli()).toString().padStart(19, '0')
}

enum class OutputType(val keyName: String) {
    SYSTEM_OUT("out"),
    SYSTEM_ERR("err"),
}
