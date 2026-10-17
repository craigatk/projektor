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
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import java.util.UUID

/**
 * Stores each test run's coverage as a run document listing the coverage groups and their stats,
 * plus one document per group holding the group's files.
 */
class CoverageObjectStoreRepository(private val store: JsonObjectStore) : CoverageRepository {
    override suspend fun createOrGetCoverageRun(publicId: PublicId) {
        withContext(Dispatchers.IO) {
            store.create(ObjectStoreKeys.coverageRun(publicId), CoverageRunDocument(listOf()))
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
    }

    override suspend fun upsertCoverageGroup(
        publicId: PublicId,
        coverageReport: CoverageReport,
        newLineStat: CoverageStat,
    ): CoverageGroupStatus =
        withContext(Dispatchers.IO) {
            lateinit var coverageGroupStatus: CoverageGroupStatus

            updateCoverageRun(publicId) { current ->
                val existingGroup = current.groups.find { it.name == coverageReport.name }

                if (existingGroup == null) {
                    coverageGroupStatus = CoverageGroupStatus.NEW

                    val newGroup =
                        CoverageGroupDocument(UUID.randomUUID().toString(), coverageReport.name, coverageReport.totalStats.toDocument())
                    current.copy(groups = current.groups + newGroup)
                } else {
                    coverageGroupStatus = CoverageGroupStatus.EXISTING

                    val updatedGroup = existingGroup.copy(stats = lineOnlyStats(newLineStat))
                    current.copy(groups = current.groups.map { if (it.id == existingGroup.id) updatedGroup else it })
                }
            }

            coverageGroupStatus
        }

    override suspend fun insertCoverageFiles(
        publicId: PublicId,
        groupName: String,
        coverageReportFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            val group = fetchExistingGroup(publicId, groupName)

            updateGroupFiles(publicId, group) { files -> files + coverageReportFiles.map { it.toDocument() } }
        }
    }

    override suspend fun upsertCoverageFiles(
        publicId: PublicId,
        groupName: String,
        coverageReportFiles: List<CoverageFile>,
    ) {
        withContext(Dispatchers.IO) {
            val group = fetchExistingGroup(publicId, groupName)

            updateGroupFiles(publicId, group) { files ->
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

                updatedFiles + newFiles.map { it.toDocument() }
            }
        }
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

    override suspend fun deleteCoverage(publicId: PublicId): Boolean =
        withContext(Dispatchers.IO) {
            val coverageExisted = store.read<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId)) != null

            store.deletePrefix(ObjectStoreKeys.coveragePrefix(publicId))

            coverageExisted
        }

    private suspend fun fetchCoverageRun(publicId: PublicId): CoverageRunDocument? =
        withContext(Dispatchers.IO) {
            store.read<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId))
        }

    private suspend fun fetchExistingGroup(
        publicId: PublicId,
        groupName: String,
    ): CoverageGroupDocument =
        fetchCoverageRun(publicId)?.groups?.find { it.name == groupName }
            ?: throw IllegalStateException("No coverage group $groupName for test run $publicId")

    private fun updateCoverageRun(
        publicId: PublicId,
        transform: (CoverageRunDocument) -> CoverageRunDocument,
    ) {
        store.update<CoverageRunDocument>(ObjectStoreKeys.coverageRun(publicId)) { current ->
            transform(current ?: CoverageRunDocument(listOf()))
        }
    }

    private fun updateGroupFiles(
        publicId: PublicId,
        group: CoverageGroupDocument,
        transform: (List<CoverageFileDocument>) -> List<CoverageFileDocument>,
    ) {
        store.update<CoverageFilesDocument>(ObjectStoreKeys.coverageGroupFiles(publicId, group.id)) { current ->
            CoverageFilesDocument(transform(current?.files.orEmpty()))
        }
    }

    companion object {
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
