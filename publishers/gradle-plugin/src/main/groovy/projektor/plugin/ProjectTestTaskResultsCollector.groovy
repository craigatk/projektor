package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.Test

class ProjectTestTaskResultsCollector {
    private final Collection<Test> testTasks
    private final Logger logger

    ProjectTestTaskResultsCollector(Collection<Test> testTasks, Logger logger) {
        this.testTasks = testTasks
        this.logger = logger
    }

    static ProjectTestTaskResultsCollector fromAllTasks(Collection<Task> allTasks, Logger logger) {
        new ProjectTestTaskResultsCollector(allTasks.findAll { it instanceof Test }, logger)
    }

    boolean hasTestTasks() {
        !testTasks.empty
    }

    int testTaskCount() {
        testTasks.size()
    }

    String createResultsBlob() {
        List<File> junitXmlDestinationDirectories = testTasks.collect { it.reports.junitXml.destination }

        ProjektorResultsCollector resultsCollector = new ProjektorResultsCollector(logger)
        String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectories(junitXmlDestinationDirectories)

        return resultsBlob
    }
}
