package projektor.plugin

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ProjektorManualPublishTask extends AbstractTask {

    @Input
    String serverUrl

    @TaskAction
    void publish() {
        ProjectTestTaskResultsCollector projectTestTaskResultsCollector = ProjectTestTaskResultsCollector.fromAllTasks(
                project.getAllTasks(false).get(project),
                logger
        )

        if (projectTestTaskResultsCollector.hasTestGroups()) {
            String resultsBlob = projectTestTaskResultsCollector.createResultsBlob()

            ProjektorResultsClient resultsClient = new ProjektorResultsClient(serverUrl, logger)
            PublishResult publishResult = resultsClient.sendResultsToServer(resultsBlob)

            new ResultsLogger(logger).logReportResults(publishResult)
        } else {
            logger.info("No test tasks found in project ${project.name}")
        }
    }
}
