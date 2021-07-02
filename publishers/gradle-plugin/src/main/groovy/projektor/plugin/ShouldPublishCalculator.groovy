package projektor.plugin

import org.gradle.BuildResult
import org.gradle.api.logging.Logger

import static projektor.plugin.MetadataResolver.isCI

class ShouldPublishCalculator {
    static boolean shouldPublishResults(
            ProjektorPublishPluginExtension extension,
            BuildResult buildResult,
            boolean resultsDataExists,
            boolean coverageTasksExecuted,
            Map<String, String> environment,
            Logger logger
    ) {
        boolean ci = isCI(environment, extension)
        boolean buildFailed = buildResult.failure != null

        logger.info("Should publish calculation: CI=$ci, buildFailed=$buildFailed, resultsDataExists=$resultsDataExists, coverageTasksExecuted=$coverageTasksExecuted")

        if (resultsDataExists || coverageTasksExecuted) {
            return extension.alwaysPublish ||
                    (ci && extension.alwaysPublishInCI) ||
                    (!ci && extension.publishOnLocalFailure && buildFailed && resultsDataExists)
        } else {
            return false
        }
    }
}
