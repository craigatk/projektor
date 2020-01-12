package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.Test
import projektor.plugin.results.ProjektorResultsCollector

class ProjectTestTaskResultsCollector {
    private final Collection<TestGroup> testGroups
    private final Logger logger

    ProjectTestTaskResultsCollector(Collection<TestGroup> testGroups, Logger logger) {
        this.testGroups = testGroups
        this.logger = logger
    }

    static ProjectTestTaskResultsCollector fromAllTasks(Collection<Task> allTasks, Logger logger) {
        Collection<Test> allTestTasks = allTasks.findAll { it instanceof Test }
        Collection<TestGroup> testGroups = allTestTasks.collect { TestGroup.fromTask(it) }

        return new ProjectTestTaskResultsCollector(testGroups, logger)
    }

    boolean hasTestGroups() {
        !testGroups.empty
    }

    int testGroupsCount() {
        testGroups.size()
    }

    String createResultsBlob() {
        List<File> junitXmlDestinationDirectories = testGroups
                .collect { it.task }
                .collect { it.reports.junitXml.destination }

        ProjektorResultsCollector resultsCollector = new ProjektorResultsCollector(logger)
        String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectories(junitXmlDestinationDirectories)

        return resultsBlob
    }
}
