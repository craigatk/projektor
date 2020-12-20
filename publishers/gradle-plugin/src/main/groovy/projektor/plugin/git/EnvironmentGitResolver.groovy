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
        String ref = environmentResolver.findFirstEnvironmentValue(config.refEnvironmentVariables)

        if (ref != null) {
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
}
