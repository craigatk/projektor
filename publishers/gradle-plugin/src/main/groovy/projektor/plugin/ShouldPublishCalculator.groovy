package projektor.plugin

import org.gradle.BuildResult

import static projektor.plugin.MetadataResolver.isCI

class ShouldPublishCalculator {
    static boolean shouldPublishResults(
            ProjektorPublishPluginExtension extension,
            BuildResult buildResult,
            boolean resultsDataExists,
            boolean coverageTasksExecuted,
            Map<String, String> environment
    ) {
        boolean ci = isCI(environment, extension)
        boolean buildFailed = buildResult.failure != null

        if (resultsDataExists || coverageTasksExecuted) {
            return extension.alwaysPublish ||
                    (ci && extension.alwaysPublishInCI) ||
                    (!ci && extension.publishOnLocalFailure && buildFailed && resultsDataExists)
        } else {
            return false
        }
    }
}
