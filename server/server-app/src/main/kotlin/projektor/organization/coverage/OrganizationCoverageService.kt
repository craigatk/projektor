package projektor.organization.coverage

import projektor.coverage.CoverageService
import projektor.server.api.organization.OrganizationCoverage
import projektor.server.api.organization.RepositoryCoverage

class OrganizationCoverageService(
    private val organizationCoverageRepository: OrganizationCoverageRepository,
    private val coverageService: CoverageService
) {
    suspend fun getCoverage(orgName: String): OrganizationCoverage {
        val repositoryTestRuns = findReposWithCoverage(orgName)

        val repositories = repositoryTestRuns.map { repositoryTestRun ->
            val coverage = coverageService.getCoverage(repositoryTestRun.publicId)

            RepositoryCoverage(
                    publicId = repositoryTestRun.publicId.id,
                    repoName = repositoryTestRun.repoName,
                    coverage = coverage
            )
        }

        return OrganizationCoverage(repositories)
    }

    suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
            organizationCoverageRepository.findReposWithCoverage(orgName)
}
