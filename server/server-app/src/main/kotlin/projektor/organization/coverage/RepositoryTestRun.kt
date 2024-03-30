package projektor.organization.coverage

import projektor.server.api.PublicId
import java.time.Instant

data class RepositoryTestRun(val repoName: String, val publicId: PublicId, val createdTimestamp: Instant, val projectName: String?, val branchName: String?)
