package projektor.quality

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.CodeQualityReportDocument
import projektor.persistence.objectstore.CodeQualityReportsDocument
import projektor.persistence.objectstore.ObjectStoreKeys
import projektor.server.api.PublicId
import projektor.server.api.quality.CodeQualityReport

class CodeQualityReportObjectStoreRepository(private val store: JsonObjectStore) : CodeQualityReportRepository {
    override suspend fun insertCodeQualityReports(
        publicId: PublicId,
        codeQualityReports: List<CodeQualityReport>,
    ) {
        withContext(Dispatchers.IO) {
            store.update<CodeQualityReportsDocument>(ObjectStoreKeys.codeQualityReports(publicId)) { current ->
                CodeQualityReportsDocument(
                    current?.reports.orEmpty() +
                        codeQualityReports.map {
                            CodeQualityReportDocument(
                                it.idx,
                                it.contents,
                                it.fileName,
                                it.groupName,
                            )
                        },
                )
            }
        }
    }

    override suspend fun fetchCodeQualityReports(publicId: PublicId): List<CodeQualityReport> =
        withContext(Dispatchers.IO) {
            store.read<CodeQualityReportsDocument>(ObjectStoreKeys.codeQualityReports(publicId))
                ?.reports
                ?.map { CodeQualityReport(it.idx, it.contents, it.fileName, it.groupName) }
                ?: listOf()
        }
}
