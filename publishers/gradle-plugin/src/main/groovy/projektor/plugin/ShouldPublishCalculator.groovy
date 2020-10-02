package projektor.plugin

import org.gradle.BuildResult

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

    static boolean isCI(Map<String, String> environment, ProjektorPublishPluginExtension extension) {
        return extension.ciEnvironmentVariables.any {envVariable ->
            environment.get(envVariable) != null && environment.get(envVariable) != "false"
        }
    }
}
