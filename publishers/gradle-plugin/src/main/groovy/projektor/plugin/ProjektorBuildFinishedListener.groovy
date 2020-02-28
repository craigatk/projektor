package projektor.plugin

import okhttp3.OkHttpClient
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.file.FileTree
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import projektor.plugin.attachments.AttachmentsClient
import projektor.plugin.attachments.AttachmentsPublisher
import projektor.plugin.results.ResultsClient
import projektor.plugin.client.ClientConfig
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorBuildFinishedListener implements BuildListener {

    private final ClientConfig clientConfig
    private final Logger logger
    private final boolean publishOnFailureOnly
    private final File projectDir
    private final List<String> additionalResultsDirs
    private final List<FileTree> attachments
    private final ProjektorTaskFinishedListener projektorTaskFinishedListener
    private final OkHttpClient okHttpClient = new OkHttpClient()

    ProjektorBuildFinishedListener(
            ClientConfig clientConfig,
            Logger logger,
            boolean publishOnFailureOnly,
            File projectDir,
            List<String> additionalResultsDirs,
            List<FileTree> attachments,
            ProjektorTaskFinishedListener projektorTaskFinishedListener
    ) {
        this.clientConfig = clientConfig
        this.logger = logger
        this.publishOnFailureOnly = publishOnFailureOnly
        this.projectDir = projectDir
        this.additionalResultsDirs = additionalResultsDirs
        this.attachments = attachments
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

            ResultsClient resultsClient = new ResultsClient(okHttpClient, clientConfig, logger)
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)

            if (attachments) {
                AttachmentsClient attachmentsClient = new AttachmentsClient(okHttpClient, clientConfig, logger)
                new AttachmentsPublisher(attachmentsClient, logger).publishAttachments(publishResult.publicId, attachments)
            }
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
