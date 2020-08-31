package projektor.plugin.git

import org.gradle.api.logging.Logger
import projektor.plugin.results.grouped.GitMetadata

class GitMetadataFinder {
    static GitMetadata findGitMetadata(GitResolutionConfig config, Logger logger) {
        return new EnvironmentGitResolver(config, new EnvironmentResolver(), logger).createMetadata() ?:
                new JGitResolver(config, logger).createMetadata()
    }
}
