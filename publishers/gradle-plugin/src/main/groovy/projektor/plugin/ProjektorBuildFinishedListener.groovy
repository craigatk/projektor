package projektor.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import projektor.plugin.results.ProjektorResultsClient
import projektor.plugin.results.ResultsClientConfig
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorBuildFinishedListener implements BuildListener {

    private final ResultsClientConfig resultsClientConfig
    private final Logger logger
    private final boolean publishOnFailureOnly
    private final File projectDir
    private final List<String> additionalResultsDirs
    private final ProjektorTaskFinishedListener projektorTaskFinishedListener

    ProjektorBuildFinishedListener(
            ResultsClientConfig resultsClientConfig,
            Logger logger,
            boolean publishOnFailureOnly,
            File projectDir,
            List<String> additionalResultsDirs,
            ProjektorTaskFinishedListener projektorTaskFinishedListener
    ) {
        this.resultsClientConfig = resultsClientConfig
        this.logger = logger
        this.publishOnFailureOnly = publishOnFailureOnly
        this.projectDir = projectDir
        this.additionalResultsDirs = additionalResultsDirs
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
        List<TestGroup> testGroupsFromAdditionalDirs = TestDirectoryGroup.listFromDirPaths(projectDir, additionalResultsDirs)
        ProjectTestResultsCollector projectTestResultsCollector = new ProjectTestResultsCollector(
                this.projektorTaskFinishedListener.testGroups + testGroupsFromAdditionalDirs,
                logger
        )

        if (projectTestResultsCollector.hasTestGroups()) {
            logger.info("Build finished, gathering and publishing Projektor test reports from " +
                    "${projectTestResultsCollector.testGroupsCount()} test tasks")
            GroupedResults groupedResults = projectTestResultsCollector.createGroupedResults()

            ProjektorResultsClient resultsClient = new ProjektorResultsClient(resultsClientConfig, logger)
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)
        } else {
            logger.info("Projektor plugin applied but no test results found in this build")
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
