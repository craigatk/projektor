package projektor.plugin.git

import projektor.plugin.ProjektorPublishPluginExtension

class GitResolutionConfig {
    boolean enabled
    List<String> mainBranchNames
    List<String> repoEnvironmentVariables
    List<String> refEnvironmentVariables

    static GitResolutionConfig fromExtension(ProjektorPublishPluginExtension extension) {
        return new GitResolutionConfig(
                enabled: extension.gitInfoEnabled,
                mainBranchNames: extension.gitMainBranchNames,
                repoEnvironmentVariables: extension.gitRepoEnvironmentVariables,
                refEnvironmentVariables: extension.gitRefEnvironmentVariables
        )
    }
}
