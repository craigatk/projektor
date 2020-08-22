package projektor.plugin

import org.gradle.BuildResult

class ShouldPublishCalculator {
    static boolean shouldPublishResults(
            ProjektorPublishPluginExtension extension,
            BuildResult buildResult,
            boolean coverageTasksExecuted,
            Map<String, String> environment
    ) {
        return shouldPublishBasedOnBuildResult(extension, buildResult) ||
                shouldPublishBasedOnCoverage(extension, coverageTasksExecuted, environment)
    }

    private static boolean shouldPublishBasedOnBuildResult(
            ProjektorPublishPluginExtension extension,
            BuildResult buildResult
    ) {
        return !extension.autoPublishOnFailureOnly || buildResult.failure != null
    }

    private static boolean shouldPublishBasedOnCoverage(
            ProjektorPublishPluginExtension extension,
            boolean coverageTasksExecuted,
            Map<String, String> environment
    ) {
        return extension.codeCoveragePublish &&
                extension.autoPublishWhenCoverageInCI &&
                coverageTasksExecuted &&
                environment.containsKey("CI")
    }
}
