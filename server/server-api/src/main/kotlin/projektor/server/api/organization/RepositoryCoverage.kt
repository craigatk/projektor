package projektor.server.api.organization

import projektor.server.api.coverage.Coverage

data class RepositoryCoverage(
    val publicId: String,
    val repoName: String,
    val coverage: Coverage?
)
