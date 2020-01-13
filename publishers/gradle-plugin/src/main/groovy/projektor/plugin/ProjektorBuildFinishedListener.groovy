package projektor.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import projektor.plugin.results.ProjektorResultsClient
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorBuildFinishedListener implements BuildListener {

    private final String serverUrl
    private final Logger logger
    private final boolean publishOnFailureOnly
    private final ProjektorTaskFinishedListener projektorTaskFinishedListener

    ProjektorBuildFinishedListener(
            String serverUrl,
            Logger logger,
            boolean publishOnFailureOnly,
            ProjektorTaskFinishedListener projektorTaskFinishedListener
    ) {
        this.serverUrl = serverUrl
        this.logger = logger
        this.publishOnFailureOnly = publishOnFailureOnly
        this.projektorTaskFinishedListener = projektorTaskFinishedListener
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        boolean shouldPublish = !this.publishOnFailureOnly || buildResult.failure != null

        if (shouldPublish) {
            collectAndPublishResults()
        } else {
            logger.info("Projektor set to auto-publish only on failure and tests passed")
        }
    }

    private void collectAndPublishResults() {
        ProjectTestTaskResultsCollector projectTestTaskResultsCollector = new ProjectTestTaskResultsCollector(
                this.projektorTaskFinishedListener.testGroups,
                logger
        )

        if (projectTestTaskResultsCollector.hasTestGroups()) {
            logger.info("Build finished, gathering and publishing Projektor test reports from " +
                    "${projectTestTaskResultsCollector.testGroupsCount()} test tasks")
            GroupedResults groupedResults = projectTestTaskResultsCollector.createGroupedResults()

            ProjektorResultsClient resultsClient = new ProjektorResultsClient(serverUrl, logger)
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)
        } else {
            logger.info("Projektor plugin applied but no test tasks executed in this build")
        }
    }

    // Don't need to listen to the other methods

    @Override
    void buildStarted(Gradle gradle) { }

    @Override
    void settingsEvaluated(Settings settings) { }

    @Override
    void projectsLoaded(Gradle gradle) { }

    @Override
    void projectsEvaluated(Gradle gradle) { }
}
