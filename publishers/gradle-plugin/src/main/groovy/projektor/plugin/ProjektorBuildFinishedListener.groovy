package projektor.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import projektor.plugin.attachments.AttachmentsClient
import projektor.plugin.attachments.AttachmentsPublisher
import projektor.plugin.client.ResultsClient
import projektor.plugin.client.ClientConfig
import projektor.plugin.coverage.CodeCoverageTaskCollector
import projektor.plugin.git.GitMetadataFinder
import projektor.plugin.git.GitResolutionConfig
import projektor.plugin.notification.NotificationConfig
import projektor.plugin.notification.link.LinkMessageWriter
import projektor.plugin.notification.link.LinkModel
import projektor.plugin.notification.slack.SlackMessageBuilder
import projektor.plugin.notification.slack.SlackMessageWriter
import projektor.plugin.notification.slack.message.SlackAttachmentsMessage
import projektor.plugin.quality.CodeQualityCollector
import projektor.plugin.quality.CodeQualityFilePayload
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.ResultsMetadata

import static projektor.plugin.MetadataResolver.findBuildNumber
import static projektor.plugin.MetadataResolver.isCI

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
        Collection<Task> allTasks = []

        try {
            allTasks = buildResult.gradle.taskGraph.allTasks
        } catch (IllegalStateException e) {
            logger.info("Task graph is not ready", e)
        }

        CodeCoverageTaskCollector codeCoverageTaskCollector = new CodeCoverageTaskCollector(
                allTasks,
                coverageEnabled,
                logger
        )

        List<TestGroup> testGroupsFromAdditionalDirs = TestDirectoryGroup.listFromDirPaths(projectDir, additionalResultsDirs)
        ProjectTestResultsCollector projectTestResultsCollector = new ProjectTestResultsCollector(
                this.projektorTaskFinishedListener.testGroups + testGroupsFromAdditionalDirs,
                logger
        )

        CodeQualityCollector codeQualityCollector = new CodeQualityCollector(logger)
        List<CodeQualityFilePayload> codeQualityFiles = codeQualityCollector.collectCodeQualityFiles(extension.codeQualityReports)

        boolean shouldPublish = ShouldPublishCalculator.shouldPublishResults(
                extension,
                buildResult,
                projectTestResultsCollector.hasTestGroups(),
                codeCoverageTaskCollector.hasCodeCoverageData(),
                !codeQualityFiles.empty,
                System.getenv(),
                logger
        )

        if (shouldPublish) {
            collectAndPublishResults(
                    buildResult,
                    projectTestResultsCollector,
                    codeCoverageTaskCollector,
                    codeQualityFiles
            )
        } else {
            logger.info("Projektor set to auto-publish only on failure and tests passed")
        }
    }

    private void collectAndPublishResults(
            BuildResult buildResult,
            ProjectTestResultsCollector projectTestResultsCollector,
            CodeCoverageTaskCollector codeCoverageTaskCollector,
            List<CodeQualityFilePayload> codeQualityFiles
    ) {
        String buildNumber = findBuildNumber(System.getenv(), extension)
        boolean isCI = isCI(System.getenv(), extension)
        String group = extension.groupResults ? buildNumber : null

        logger.info("Build finished, gathering and publishing Projektor test reports from " +
                "${projectTestResultsCollector.testGroupsCount()} test tasks")
        GroupedResults groupedResults = projectTestResultsCollector.createGroupedResults()
        groupedResults.metadata = new ResultsMetadata(
                git: GitMetadataFinder.findGitMetadata(gitResolutionConfig, logger),
                ci: isCI,
                group: group
        )
        groupedResults.wallClockDuration = projektorTaskFinishedListener.testWallClockDurationInSeconds

        if (coverageEnabled) {
            groupedResults.coverageFiles = codeCoverageTaskCollector.coverageFilePayloads
        }

        if (codeQualityFiles) {
            groupedResults.codeQualityFiles = codeQualityFiles
        }

        ResultsClient resultsClient = new ResultsClient(clientConfig, logger)
        PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

        new ResultsLogger(logger).logReportResults(publishResult)

        writeNotifications(buildResult, publishResult, isCI)

        if (attachments) {
            AttachmentsClient attachmentsClient = new AttachmentsClient(clientConfig, logger)
            new AttachmentsPublisher(attachmentsClient, logger).publishAttachments(publishResult.publicId, attachments)
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
    void settingsEvaluated(Settings settings) { }

    @Override
    void projectsLoaded(Gradle gradle) { }

    @Override
    void projectsEvaluated(Gradle gradle) { }
}
