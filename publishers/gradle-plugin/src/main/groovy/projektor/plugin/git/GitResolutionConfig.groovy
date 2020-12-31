package projektor.plugin.git

import projektor.plugin.ProjektorPublishPluginExtension

class GitResolutionConfig {
    boolean enabled
    List<String> mainBranchNames
    List<String> repoEnvironmentVariables
    List<String> refEnvironmentVariables
    List<String> branchEnvironmentVariables
    List<String> commitShaEnvironmentVariables
    List<String> pullRequestNumberEnvironmentVariables

    static GitResolutionConfig fromExtension(ProjektorPublishPluginExtension extension) {
        return new GitResolutionConfig(
                enabled: extension.gitInfoEnabled,
                mainBranchNames: extension.gitMainBranchNames,
                repoEnvironmentVariables: extension.gitRepoEnvironmentVariables,
                refEnvironmentVariables: extension.gitRefEnvironmentVariables,
                branchEnvironmentVariables: extension.gitBranchEnvironmentVariables,
                commitShaEnvironmentVariables: extension.gitCommitShaEnvironmentVariables,
                pullRequestNumberEnvironmentVariables: extension.gitPullRequestNumberEnvironmentVariables
        )
    }
}
