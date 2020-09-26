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
        return shouldPublishBasedOnBuildResult(resultsDataExists, extension, buildResult) ||
                shouldPublishBasedOnCoverage(extension, coverageTasksExecuted, environment)
    }

    static boolean isCI(Map<String, String> environment) {
        return environment.get("CI") != null && environment.get("CI") != "false"
    }

    private static boolean shouldPublishBasedOnBuildResult(
            boolean resultsDataExists,
            ProjektorPublishPluginExtension extension,
            BuildResult buildResult
    ) {
        return resultsDataExists && (!extension.autoPublishOnFailureOnly || buildResult.failure != null)
    }

    private static boolean shouldPublishBasedOnCoverage(
            ProjektorPublishPluginExtension extension,
            boolean coverageTasksExecuted,
            Map<String, String> environment
    ) {
        return extension.codeCoveragePublish &&
                extension.autoPublishWhenCoverageInCI &&
                coverageTasksExecuted &&
                isCI(environment)
    }
}
