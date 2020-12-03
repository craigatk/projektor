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
import projektor.plugin.coverage.CodeCoverageFile
import projektor.plugin.coverage.CodeCoverageTaskCollector
import projektor.plugin.git.GitMetadataFinder
import projektor.plugin.git.GitResolutionConfig
import projektor.plugin.notification.NotificationConfig
import projektor.plugin.notification.link.LinkMessageWriter
import projektor.plugin.notification.link.LinkModel
import projektor.plugin.notification.slack.SlackMessageBuilder
import projektor.plugin.notification.slack.SlackMessageWriter
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.ResultsMetadata

import static projektor.plugin.ShouldPublishCalculator.isCI

class ProjektorBuildFinishedListener implements BuildListener {

    private final ClientConfig clientConfig
    private final NotificationConfig notificationConfig
    private final Logger logger
    private final ProjektorPublishPluginExtension extension
    private final File projectDir
    private final List<String> additionalResultsDirs
    private final List<FileTree> attachments
    private final ProjektorTaskFinishedListener projektorTaskFinishedListener
    private final boolean coverageEnabled
    private final GitResolutionConfig gitResolutionConfig

    ProjektorBuildFinishedListener(
            ClientConfig clientConfig,
            NotificationConfig notificationConfig,
            Logger logger,
            File projectDir,
            ProjektorPublishPluginExtension extension,
            ProjektorTaskFinishedListener projektorTaskFinishedListener
    ) {
        this.clientConfig = clientConfig
        this.notificationConfig = notificationConfig
        this.logger = logger
        this.extension = extension
        this.projectDir = projectDir
        this.additionalResultsDirs = extension.additionalResultsDirs
        this.attachments = extension.attachments
        this.projektorTaskFinishedListener = projektorTaskFinishedListener
        this.coverageEnabled = extension.codeCoveragePublish
        this.gitResolutionConfig = GitResolutionConfig.fromExtension(extension)
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        CodeCoverageTaskCollector codeCoverageTaskCollector = new CodeCoverageTaskCollector(
                buildResult,
                coverageEnabled,
                logger
        )

        List<TestGroup> testGroupsFromAdditionalDirs = TestDirectoryGroup.listFromDirPaths(projectDir, additionalResultsDirs)
        ProjectTestResultsCollector projectTestResultsCollector = new ProjectTestResultsCollector(
                this.projektorTaskFinishedListener.testGroups + testGroupsFromAdditionalDirs,
                logger
        )

        boolean shouldPublish = ShouldPublishCalculator.shouldPublishResults(
                extension,
                buildResult,
                projectTestResultsCollector.hasTestGroups(),
                codeCoverageTaskCollector.hasCodeCoverageData(),
                System.getenv()
        )

        if (shouldPublish) {
            collectAndPublishResults(buildResult, projectTestResultsCollector, codeCoverageTaskCollector)
        } else {
            logger.info("Projektor set to auto-publish only on failure and tests passed")
        }
    }

    private void collectAndPublishResults(
            BuildResult buildResult,
            ProjectTestResultsCollector projectTestResultsCollector,
            CodeCoverageTaskCollector codeCoverageTaskCollector
    ) {
        boolean isCI = isCI(System.getenv(), extension)

        logger.info("Build finished, gathering and publishing Projektor test reports from " +
                "${projectTestResultsCollector.testGroupsCount()} test tasks")
        GroupedResults groupedResults = projectTestResultsCollector.createGroupedResults()
        groupedResults.metadata = new ResultsMetadata(
                git: GitMetadataFinder.findGitMetadata(gitResolutionConfig, logger),
                ci: isCI
        )
        groupedResults.wallClockDuration = projektorTaskFinishedListener.testWallClockDurationInSeconds

        ResultsClient resultsClient = new ResultsClient(clientConfig, logger)
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        new ResultsLogger(logger).logReportResults(publishResult)

        writeNotifications(buildResult, publishResult, isCI)

        if (attachments) {
            AttachmentsClient attachmentsClient = new AttachmentsClient(clientConfig, logger)
            new AttachmentsPublisher(attachmentsClient, logger).publishAttachments(publishResult.publicId, attachments)
        }

        if (coverageEnabled) {
            CoverageClient coverageClient = new CoverageClient(clientConfig, logger)

            List<CodeCoverageFile> codeCoverageGroups = codeCoverageTaskCollector.codeCoverageFiles

            logger.info("Publishing ${codeCoverageGroups.size()} code coverage reports to Projektor server")

            codeCoverageGroups.forEach { CodeCoverageFile coverageGroup ->
                coverageClient.sendCoverageToServer(coverageGroup, publishResult.publicId)
            }
        }
    }

    private void writeNotifications(BuildResult buildResult, PublishResult publishResult, boolean isCI) {
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

        if (notificationConfig.writeLinkFile && isCI) {
            new LinkMessageWriter().writeLinkFile(
                    new LinkModel(reportUrl: publishResult.reportUrl, id: publishResult.publicId),
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
