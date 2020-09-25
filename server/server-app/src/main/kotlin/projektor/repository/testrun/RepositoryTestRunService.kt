package projektor.repository.testrun

class RepositoryTestRunService(private val repositoryTestRunRepository: RepositoryTestRunRepository) {

    suspend fun fetchRepositoryTestRunTimeline(repoName: String, projectName: String?) =
            repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName)
}
