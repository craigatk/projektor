package projektor.plugin

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import projektor.plugin.results.ProjektorResultsClient
import projektor.plugin.results.ResultsClientConfig
import projektor.plugin.results.ResultsLogger
import projektor.plugin.results.grouped.GroupedResults

class ProjektorManualPublishTask extends AbstractTask {

    @Input
    String serverUrl

    @Input
    @Optional
    String publishToken

    @TaskAction
    void publish() {
        ProjectTestTaskResultsCollector projectTestTaskResultsCollector = ProjectTestTaskResultsCollector.fromAllTasks(
                project.getAllTasks(false).get(project),
                logger
        )

        if (projectTestTaskResultsCollector.hasTestGroups()) {
            GroupedResults groupedResults = projectTestTaskResultsCollector.createGroupedResults()

            ProjektorResultsClient resultsClient = new ProjektorResultsClient(
                    new ResultsClientConfig(serverUrl, java.util.Optional.ofNullable(publishToken)),
                    logger
            )
            PublishResult publishResult = resultsClient.sendResultsToServer(groupedResults)

            new ResultsLogger(logger).logReportResults(publishResult)
        } else {
            logger.info("No test tasks found in project ${project.name}")
        }
    }
}
