package projektor.plugin.git

import org.gradle.api.logging.Logger
import projektor.plugin.results.grouped.GitMetadata

abstract class GitResolver {

    protected final GitResolutionConfig config
    protected final Logger logger

    GitResolver(GitResolutionConfig config, Logger logger) {
        this.config = config
        this.logger = logger
    }

    abstract String findBranchName()

    abstract String findRepository()

    abstract String findCommitSha()

    abstract Integer findPullRequestNumber()

    boolean isMainBranch() {
        String branchName = findBranchName()

        return branchName != null && config.mainBranchNames.contains(branchName)
    }

    GitMetadata createMetadata() {
        if (config.isEnabled()) {
            String repoName = findRepository()

            if (repoName != null) {
                GitMetadata gitMetadata = new GitMetadata(
                        repoName: findRepository(),
                        branchName: findBranchName(),
                        isMainBranch: isMainBranch(),
                        commitSha: findCommitSha(),
                        pullRequestNumber: findPullRequestNumber()
                )

                logger.info("Projektor sending Git metadata $gitMetadata")

                return gitMetadata
            } else {
                return null
            }
        } else {
            return null
        }
    }
}
