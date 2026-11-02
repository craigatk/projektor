package projektor.organization.coverage

import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.mapParallel
import projektor.server.api.PublicId

class OrganizationCoverageObjectStoreRepository(private val testRunIndex: TestRunIndex) : OrganizationCoverageRepository {
    override suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
        testRunIndex.readOrganizationRepositoryNames(orgName)
            .mapParallel { repoName ->
                // The most recent mainline run with coverage for each of the repo's projects
                testRunIndex.readRepositoryEntries(repoName)
                    .filter { it.isMainBranch && it.coverageStats != null }
                    .groupBy { it.projectName }
                    .values
                    .map { projectEntries ->
                        val mostRecentEntry = projectEntries.last()

                        RepositoryTestRun(
                            repoName = repoName,
                            publicId = PublicId(mostRecentEntry.publicId),
                            createdTimestamp = mostRecentEntry.createdTimestamp,
                            projectName = mostRecentEntry.projectName,
                            branchName = mostRecentEntry.branchName,
                        )
                    }
            }
            .flatten()
}
