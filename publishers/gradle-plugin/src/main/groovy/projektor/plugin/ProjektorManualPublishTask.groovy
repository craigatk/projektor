package projektor.plugin

import okhttp3.OkHttpClient
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import projektor.plugin.results.ResultsClient
import projektor.plugin.client.ClientConfig
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorManualPublishTask extends AbstractTask {

    @Input
    String serverUrl

    @Input
    @Optional
    String publishToken

    @Input
    @Optional
    List<String> additionalResultsDirs = []

    @TaskAction
    void publish() {
        File projectDir = project.projectDir
        ProjectTestResultsCollector projectTestTaskResultsCollector = ProjectTestResultsCollector.fromAllTasks(
                project.getAllTasks(false).get(project),
                projectDir,
                additionalResultsDirs,
                logger
        )

        if (projectTestTaskResultsCollector.hasTestGroups()) {
            GroupedResults groupedResults = projectTestTaskResultsCollector.createGroupedResults()

            ResultsClient resultsClient = new ResultsClient(
                    new OkHttpClient(),
                    new ClientConfig(serverUrl, java.util.Optional.ofNullable(publishToken)),
                    logger
            )
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)
        } else {
            logger.info("No test tasks found in project ${project.name}")
        }
    }
}
