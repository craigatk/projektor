package projektor.server.api.organization

import projektor.server.api.PublicId
import projektor.server.api.coverage.Coverage

data class RepositoryCoverage(
    val publicId: PublicId,
    val repoName: String,
    val coverage: Coverage?
)
