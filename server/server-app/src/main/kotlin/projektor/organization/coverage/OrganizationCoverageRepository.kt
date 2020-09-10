package projektor.organization.coverage

interface OrganizationCoverageRepository {
    suspend fun findReposWithCoverage(orgName: String): List<RepositoryTestRun>
}
