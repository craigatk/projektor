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
            boolean codeQualityReportsExist,
            Map<String, String> environment,
            Logger logger
    ) {
        boolean ci = isCI(environment, extension)
        boolean buildFailed = buildResult.failure != null

        boolean shouldPublish = false

        if (codeQualityReportsExist && (buildFailed || ci)) {
            shouldPublish = true
        } else if (resultsDataExists || coverageTasksExecuted) {
            shouldPublish = extension.alwaysPublish ||
                    (ci && extension.alwaysPublishInCI) ||
                    (!ci && extension.publishOnLocalFailure && buildFailed && resultsDataExists)
        }

        logger.info("Should publish calculation $shouldPublish : CI=$ci, buildFailed=$buildFailed, resultsDataExists=$resultsDataExists, coverageTasksExecuted=$coverageTasksExecuted, codeQualityReportsExist=$codeQualityReportsExist")

        return shouldPublish
    }
}
