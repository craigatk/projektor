package projektor.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.persistence.objectstore.CoverageFileDocument
import projektor.persistence.objectstore.CoverageFilesDocument
import projektor.persistence.objectstore.CoverageGroupDocument
import projektor.persistence.objectstore.CoverageRunDocument
import projektor.persistence.objectstore.CoverageStatsDocument
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.persistence.objectstore.TestRunIndex
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import java.util.UUID

/**
 * Stores each test run's coverage as a run document listing the coverage groups and their stats,
 * plus one document per group holding the group's files.
 */
class CoverageObjectStoreRepository(
    private val store: JsonObjectStore,
    private val testRunIndex: TestRunIndex,
) : CoverageRepository {
    override suspend fun createOrGetCoverageRun(publicId: PublicId) {
        val created =
            withContext(Dispatchers.IO) {
                store.create(ObjectStoreKeys.coverageRun(publicId), CoverageRunDocument(listOf()))
            }

        if (created != null) {
            testRunIndex.refreshEntry(publicId)
        }
    }

    override suspend fun addCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        coverageReportFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            val group = CoverageGroupDocument(UUID.randomUUID().toString(), coverageReport.name, coverageReport.totalStats.toDocument())

            // Write the files before the group referencing them is visible
            store.write(
                ObjectStoreKeys.coverageGroupFiles(publicId, group.id),
                CoverageFilesDocument(coverageReportFiles.map { it.toDocument() }),
            )

            updateCoverageRun(publicId) { current -> current.copy(groups = current.groups + group) }
        }

        testRunIndex.refreshEntry(publicId)
    }

    /**
     * Each step is a compare-and-swap update of one document, so concurrent appends don't lose each other's changes:
     * 1. Find or create the group and count the append.
     * 2. Combine the report's files with the group's files.
     * 3. Recalculate the group's stats from the latest files.
     */
    override suspend fun appendCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        incomingCoverageFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            lateinit var groupId: String

            updateCoverageRun(publicId) { current ->
                val existingGroup = current.groups.find { it.name == coverageReport.name }

                if (existingGroup == null) {
                    val newGroup =
                        CoverageGroupDocument(
                            id = UUID.randomUUID().toString(),
                            name = coverageReport.name,
                            stats = coverageReport.totalStats.toDocument(),
                            createdByAppend = true,
                            appendCount = 1,
                        )
                    groupId = newGroup.id
                    current.copy(groups = current.groups + newGroup)
                } else {
                    groupId = existingGroup.id
                    current.copy(
                        groups = current.groups.map { if (it.id == existingGroup.id) it.copy(appendCount = it.appendCount + 1) else it },
                    )
                }
            }

            store.update<CoverageFilesDocument>(ObjectStoreKeys.coverageGroupFiles(publicId, groupId)) { current ->
                val currentFiles = current?.files.orEmpty()
                val combinedFiles = combineCoverageFiles(currentFiles.map { it.toApi() }, incomingCoverageFiles)

                CoverageFilesDocument(upsertFiles(currentFiles, combinedFiles))
            }

            updateCoverageRun(publicId) { current ->
                val group = current.groups.find { it.id == groupId }

                if (group == null || (group.createdByAppend && group.appendCount == 1)) {
                    null
                } else {
                    // Read inside the update so a concurrent append's files are included if it commits first
                    val files = store.read<CoverageFilesDocument>(ObjectStoreKeys.coverageGroupFiles(publicId, groupId))?.files.orEmpty()
                    val lineStat = CoverageStat(files.sumOf { it.stats.lineCovered }, files.sumOf { it.stats.lineMissed })
                    val updatedGroup = group.copy(stats = lineOnlyStats(lineStat))

                    if (updatedGroup == group) {
                        null
                    } else {
                        current.copy(
                            groups = current.groups.map { if (it.id == groupId) updatedGroup else it },
                        )
                    }
                }
            }
        }

        testRunIndex.refreshEntry(publicId)
    }

    override suspend fun fetchOverallStats(publicId: PublicId): CoverageReportStats {
        val groups = fetchCoverageRun(publicId)?.groups.orEmpty()

        return CoverageReportStats(
            statementStat = CoverageReportStat(groups.sumOf { it.stats.statementCovered }, groups.sumOf { it.stats.statementMissed }),
            lineStat = CoverageReportStat(groups.sumOf { it.stats.lineCovered }, groups.sumOf { it.stats.lineMissed }),
            branchStat = CoverageReportStat(groups.sumOf { it.stats.branchCovered }, groups.sumOf { it.stats.branchMissed }),
        )
    }

    override suspend fun fetchCoverageList(publicId: PublicId): List<CoverageReport> =
        fetchCoverageRun(publicId)?.groups.orEmpty().map { group ->
            CoverageReport(
                name = group.name,
                totalStats =
                    CoverageReportStats(
                        statementStat = CoverageReportStat(group.stats.statementCovered, group.stats.statementMissed),
                        lineStat = CoverageReportStat(group.stats.lineCovered, group.stats.lineMissed),
                        branchStat = CoverageReportStat(group.stats.branchCovered, group.stats.branchMissed),
                    ),
                files = null,
            )
        }

    override suspend fun coverageGroupExists(publicId: PublicId): Boolean = fetchCoverageRun(publicId)?.groups.orEmpty().isNotEmpty()

    override suspend fun fetchCoverageFiles(
        publicId: PublicId,
        groupName: String,
    ): List<CoverageFile> =
        coroutineScope {
            // Group names usually are unique, but a test run can have multiple groups with the same name
            fetchCoverageRun(publicId)?.groups.orEmpty()
                .filter { it.name == groupName }
                .map {
                        group ->
                    async(Dispatchers.IO) { store.read<CoverageFilesDocument>(ObjectStoreKeys.coverageGroupFiles(publicId, group.id)) }
                }
                .awaitAll()
                .flatMap { it?.files.orEmpty() }
                .map { it.toApi() }
        }

    override suspend fun deleteCoverage(publicId: PublicId): Boolean {
        val coverageExisted =
            withContext(Dispatchers.IO) {
                val coverageExisted = store.read<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId)) != null

                store.deletePrefix(ObjectStoreKeys.coveragePrefix(publicId))

                coverageExisted
            }

        testRunIndex.refreshEntry(publicId)

        return coverageExisted
    }

    private suspend fun fetchCoverageRun(publicId: PublicId): CoverageRunDocument? =
        withContext(Dispatchers.IO) {
            store.read<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId))
        }

    // The transform returns null to leave the document unchanged
    private fun updateCoverageRun(
        publicId: PublicId,
        transform: (CoverageRunDocument) -> CoverageRunDocument?,
    ) {
        store.update<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId)) { current ->
            transform(current ?: CoverageRunDocument(listOf()))
        }
    }

    companion object {
        /**
         * Adds files whose path isn't in the group yet. For files already in the group, updates their missed lines
         * and line stats (and clears their partial lines and zeroes their statement and branch stats).
         */
        private fun upsertFiles(
            files: List<CoverageFileDocument>,
            coverageReportFiles: List<CoverageFile>,
        ): List<CoverageFileDocument> {
            val currentFilePaths = files.map { it.filePath }

            val newFiles = coverageReportFiles.filter { !currentFilePaths.contains(it.filePath) }
            val existingFilesByPath =
                coverageReportFiles
                    .filter { currentFilePaths.contains(it.filePath) }
                    // Like the database's file_path = ? lookup, a file without a path is never updated
                    .filter { it.filePath != null }
                    .associateBy { it.filePath }

            val updatedFiles =
                files.map { file ->
                    val reportFile = existingFilesByPath[file.filePath]

                    if (reportFile != null) {
                        file.copy(
                            missedLines = reportFile.missedLines.toList(),
                            partialLines = listOf(),
                            stats = lineOnlyStats(reportFile.stats.lineStat),
                        )
                    } else {
                        file
                    }
                }

            return updatedFiles + newFiles.map { it.toDocument() }
        }

        // Appended coverage only tracks line stats, so the statement and branch stats are zeroed
        private fun lineOnlyStats(lineStat: CoverageStat) =
            CoverageStatsDocument(
                statementCovered = 0,
                statementMissed = 0,
                lineCovered = lineStat.covered,
                lineMissed = lineStat.missed,
                branchCovered = 0,
                branchMissed = 0,
            )

        private fun CoverageReportStats.toDocument() =
            CoverageStatsDocument(
                statementCovered = statementStat.covered,
                statementMissed = statementStat.missed,
                lineCovered = lineStat.covered,
                lineMissed = lineStat.missed,
                branchCovered = branchStat.covered,
                branchMissed = branchStat.missed,
            )

        private fun CoverageStats.toDocument() =
            CoverageStatsDocument(
                statementCovered = statementStat.covered,
                statementMissed = statementStat.missed,
                lineCovered = lineStat.covered,
                lineMissed = lineStat.missed,
                branchCovered = branchStat.covered,
                branchMissed = branchStat.missed,
            )

        private fun CoverageFile.toDocument() =
            CoverageFileDocument(
                fileName = fileName,
                directoryName = directoryName,
                filePath = filePath,
                missedLines = missedLines.toList(),
                partialLines = partialLines.toList(),
                stats = stats.toDocument(),
            )

        private fun CoverageFileDocument.toApi() =
            CoverageFile(
                fileName = fileName,
                directoryName = directoryName,
                missedLines = missedLines.toTypedArray(),
                partialLines = partialLines.toTypedArray(),
                stats =
                    CoverageStats(
                        statementStat = CoverageStat(stats.statementCovered, stats.statementMissed, null),
                        lineStat = CoverageStat(stats.lineCovered, stats.lineMissed, null),
                        branchStat = CoverageStat(stats.branchCovered, stats.branchMissed, null),
                    ),
                filePath = filePath,
            )
    }
}
