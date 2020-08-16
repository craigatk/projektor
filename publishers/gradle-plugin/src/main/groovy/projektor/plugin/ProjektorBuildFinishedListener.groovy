package projektor.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.file.FileTree
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import projektor.plugin.attachments.AttachmentsClient
import projektor.plugin.attachments.AttachmentsPublisher
import projektor.plugin.client.CoverageClient
import projektor.plugin.client.ResultsClient
import projektor.plugin.client.ClientConfig
import projektor.plugin.coverage.CodeCoverageGroup
import projektor.plugin.coverage.CodeCoverageTaskFinishedListener
import projektor.plugin.notification.NotificationConfig
import projektor.plugin.notification.slack.SlackMessageBuilder
import projektor.plugin.notification.slack.SlackMessageWriter
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorBuildFinishedListener implements BuildListener {

    private final ClientConfig clientConfig
    private final NotificationConfig notificationConfig
    private final Logger logger
    private final boolean publishOnFailureOnly
    private final File projectDir
    private final List<String> additionalResultsDirs
    private final List<FileTree> attachments
    private final ProjektorTaskFinishedListener projektorTaskFinishedListener
    private final CodeCoverageTaskFinishedListener codeCoverageTaskFinishedListener
    private final boolean coverageEnabled

    ProjektorBuildFinishedListener(
            ClientConfig clientConfig,
            NotificationConfig notificationConfig,
            Logger logger,
            File projectDir,
            ProjektorPublishPluginExtension extension,
            ProjektorTaskFinishedListener projektorTaskFinishedListener,
            CodeCoverageTaskFinishedListener codeCoverageTaskFinishedListener
    ) {
        this.clientConfig = clientConfig
        this.notificationConfig = notificationConfig
        this.logger = logger
        this.publishOnFailureOnly = extension.autoPublishOnFailureOnly
        this.projectDir = projectDir
        this.additionalResultsDirs = extension.additionalResultsDirs
        this.attachments = extension.attachments
        this.projektorTaskFinishedListener = projektorTaskFinishedListener
        this.codeCoverageTaskFinishedListener = codeCoverageTaskFinishedListener
        this.coverageEnabled = extension.codeCoveragePublish
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        boolean shouldPublish = !this.publishOnFailureOnly || buildResult.failure != null

        if (shouldPublish) {
            collectAndPublishResults(buildResult)
        } else {
            logger.info("Projektor set to auto-publish only on failure and tests passed")
        }
    }

    private void collectAndPublishResults(BuildResult buildResult) {
        List<TestGroup> testGroupsFromAdditionalDirs = TestDirectoryGroup.listFromDirPaths(projectDir, additionalResultsDirs)
        ProjectTestResultsCollector projectTestResultsCollector = new ProjectTestResultsCollector(
                this.projektorTaskFinishedListener.testGroups + testGroupsFromAdditionalDirs,
                logger
        )

        if (projectTestResultsCollector.hasTestGroups()) {
            logger.info("Build finished, gathering and publishing Projektor test reports from " +
                    "${projectTestResultsCollector.testGroupsCount()} test tasks")
            GroupedResults groupedResults = projectTestResultsCollector.createGroupedResults()

            ResultsClient resultsClient = new ResultsClient(clientConfig, logger)
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)

            writeNotifications(buildResult, publishResult)

            if (attachments) {
                AttachmentsClient attachmentsClient = new AttachmentsClient(clientConfig, logger)
                new AttachmentsPublisher(attachmentsClient, logger).publishAttachments(publishResult.publicId, attachments)
            }

            if (coverageEnabled) {
                CoverageClient coverageClient = new CoverageClient(clientConfig, logger)

                List<CodeCoverageGroup> codeCoverageGroups = codeCoverageTaskFinishedListener.codeCoverageGroups

                logger.info("Publishing ${codeCoverageGroups.size()} code coverage reports to Projektor server")

                codeCoverageGroups.forEach { CodeCoverageGroup coverageGroup ->
                    coverageClient.sendCoverageToServer(coverageGroup.reportFile, publishResult.publicId)
                }
            }
        } else {
            logger.info("Projektor plugin applied but no test results found in this build")
        }
    }

    private void writeNotifications(BuildResult buildResult, PublishResult publishResult) {
        if (notificationConfig.writeSlackMessageFile) {
            String rootProjectName = buildResult.gradle.rootProject.name
            SlackAttachmentsMessage slackAttachmentsMessage = new SlackMessageBuilder()
                .setProjectName(rootProjectName)
                .setProjektorUrl(publishResult.reportUrl)
                .buildAttachmentsMessage()

            new SlackMessageWriter().writeSlackMessage(
                    slackAttachmentsMessage,
                    notificationConfig,
                    projectDir
            )
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
