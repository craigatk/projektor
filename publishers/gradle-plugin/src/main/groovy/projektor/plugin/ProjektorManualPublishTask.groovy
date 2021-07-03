package projektor.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import projektor.plugin.attachments.AttachmentsClient
import projektor.plugin.attachments.AttachmentsPublisher
import projektor.plugin.client.ResultsClient
import projektor.plugin.client.ClientConfig
import projektor.plugin.coverage.CodeCoverageTaskCollector
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorManualPublishTask extends DefaultTask {

    @Input
    String serverUrl

    @Input
    @Optional
    String publishToken

    @Input
    @Optional
    List<String> additionalResultsDirs = []

    @InputFiles
    @Optional
    List<FileTree> attachments = []

    @Input
    @Optional
    Boolean codeCoveragePublish = true

    @Input
    @Optional
    Boolean compressionEnabled = true

    @Input
    @Optional
    Integer publishRetryMaxAttempts = 3

    @Input
    @Optional
    Long publishRetryInterval = 100

    @Input
    @Optional
    Long publishTimeout = 10_000

    @TaskAction
    void publish() {
        File projectDir = project.projectDir
        Collection<Task> allTasks = project.getAllTasks(false).get(project)

        ProjectTestResultsCollector projectTestTaskResultsCollector = ProjectTestResultsCollector.fromAllTasks(
                allTasks,
                projectDir,
                additionalResultsDirs,
                logger
        )

        if (projectTestTaskResultsCollector.hasTestGroups()) {
            ClientConfig clientConfig = new ClientConfig(
                    serverUrl,
                    compressionEnabled,
                    java.util.Optional.ofNullable(publishToken),
                    publishRetryMaxAttempts,
                    publishRetryInterval,
                    publishTimeout
            )
            GroupedResults groupedResults = projectTestTaskResultsCollector.createGroupedResults()

            CodeCoverageTaskCollector codeCoverageTaskCollector = new CodeCoverageTaskCollector(
                    allTasks,
                    codeCoveragePublish,
                    logger
            )
            groupedResults.coverageFiles = codeCoverageTaskCollector.coverageFilePayloads

            ResultsClient resultsClient = new ResultsClient(
                    clientConfig,
                    logger
            )
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)

            if (attachments) {
                AttachmentsClient attachmentsClient = new AttachmentsClient(clientConfig, logger)
                new AttachmentsPublisher(attachmentsClient, logger).publishAttachments(publishResult.publicId, attachments)
            }
        } else {
            logger.info("No test tasks found in project ${project.name}")
        }
    }
}
