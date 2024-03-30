package projektor.organization.coverage

import projektor.coverage.CoverageService
import projektor.server.api.organization.OrganizationCoverage
import projektor.server.api.organization.OrganizationCurrentCoverage
import projektor.server.api.organization.RepositoryCoverage
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage

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
                projectName = repositoryTestRun.projectName,
                coverage = coverage
            )
        }

        return OrganizationCoverage(repositories)
    }

    suspend fun getCurrentCoverage(orgName: String): OrganizationCurrentCoverage {
        val repositoryTestRuns = findReposWithCoverage(orgName)

        val repositories = repositoryTestRuns.map { repositoryTestRun ->
            val coveredPercentage = coverageService.getCoveredLinePercentage(repositoryTestRun.publicId)

            RepositoryCurrentCoverage(
                id = repositoryTestRun.publicId.id,
                createdTimestamp = repositoryTestRun.createdTimestamp,
                coveredPercentage = coveredPercentage,
                repo = repositoryTestRun.repoName,
                project = repositoryTestRun.projectName,
                branch = repositoryTestRun.branchName
            )
        }

        return OrganizationCurrentCoverage(repositories)
    }

    suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun> =
        organizationCoverageRepository.findReposWithCoverage(orgName)
}
