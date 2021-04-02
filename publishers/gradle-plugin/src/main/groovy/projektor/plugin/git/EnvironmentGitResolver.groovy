package projektor.plugin.git

import org.gradle.api.logging.Logger

class EnvironmentGitResolver extends GitResolver {
    private final EnvironmentResolver environmentResolver

    EnvironmentGitResolver(GitResolutionConfig config, EnvironmentResolver environmentResolver, Logger logger) {
        super(config, logger)
        this.environmentResolver = environmentResolver
    }

    @Override
    String findBranchName() {
        String branch = environmentResolver.findFirstEnvironmentValue(config.getBranchEnvironmentVariables())
        String ref = environmentResolver.findFirstEnvironmentValue(config.getRefEnvironmentVariables())

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
        return environmentResolver.findFirstEnvironmentValue(config.getRepoEnvironmentVariables())
    }

    @Override
    String findCommitSha() {
        return environmentResolver.findFirstEnvironmentValue(config.getCommitShaEnvironmentVariables())
    }

    @Override
    Integer findPullRequestNumber() {
        String pullRequestStr = environmentResolver.findFirstEnvironmentValue(config.getPullRequestNumberEnvironmentVariables())

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
