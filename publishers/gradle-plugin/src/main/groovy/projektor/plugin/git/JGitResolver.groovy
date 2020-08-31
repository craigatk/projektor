package projektor.plugin.git

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.logging.Logger

class JGitResolver extends GitResolver {
    private Repository repo = null

    JGitResolver(GitResolutionConfig config, Logger logger) {
        super(config, logger)
        if (config.enabled) {
            try {
                this.repo = new FileRepositoryBuilder()
                        .readEnvironment()
                        .findGitDir()
                        .build()
            } catch (Exception e) {
                logger.info("Error setting up JGit repository in Projektor", e)
            }
        }
    }

    String findBranchName() {
        return repo?.getBranch()
    }

    String findRepository() {
        return getRepoName("upstream") ?: getRepoName("origin")
    }

    private String getRepoName(String remote) {
        String repoUrl = repo?.getConfig()?.getString("remote", remote, "url")

        return repoUrl?.split(":")?.last()?.replace(".git", "")
    }
}
