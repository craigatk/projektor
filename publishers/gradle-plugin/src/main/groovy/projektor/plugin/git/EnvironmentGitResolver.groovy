package projektor.plugin.git

import org.gradle.api.logging.Logger
import projektor.plugin.EnvironmentResolver

class EnvironmentGitResolver extends GitResolver {
    private final EnvironmentResolver environmentResolver

    EnvironmentGitResolver(GitResolutionConfig config, EnvironmentResolver environmentResolver, Logger logger) {
        super(config, logger)
        this.environmentResolver = environmentResolver
    }

    @Override
    String findBranchName() {
        String branch = environmentResolver.findFirstEnvironmentValue(config.branchEnvironmentVariables)
        String ref = environmentResolver.findFirstEnvironmentValue(config.refEnvironmentVariables)

        if (branch != null) {
            return branch
        } else if (ref != null) {
            return ref.split("/").last()
        } else {
            return null
        }
    }

    @Override
    String findRepository() {
        return environmentResolver.findFirstEnvironmentValue(config.repoEnvironmentVariables)
    }

    @Override
    String findCommitSha() {
        return environmentResolver.findFirstEnvironmentValue(config.commitShaEnvironmentVariables)
    }

    @Override
    Integer findPullRequestNumber() {
        String pullRequestStr = environmentResolver.findFirstEnvironmentValue(config.pullRequestNumberEnvironmentVariables)

        if (pullRequestStr) {
            try {
                return Integer.parseInt(pullRequestStr)
            } catch (Exception ignored) {
                return null
            }
        } else {
            return null
        }
    }
}
