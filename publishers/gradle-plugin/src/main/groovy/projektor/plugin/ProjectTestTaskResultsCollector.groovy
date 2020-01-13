package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.Test
import projektor.plugin.results.ProjektorResultsCollector
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedTestSuites

class ProjectTestTaskResultsCollector {
    private final Collection<TestGroup> testGroups
    private final Logger logger
    private final ProjektorResultsCollector resultsCollector

    ProjectTestTaskResultsCollector(Collection<TestGroup> testGroups, Logger logger) {
        this.testGroups = testGroups
        this.logger = logger
        this.resultsCollector = new ProjektorResultsCollector(logger)
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

    GroupedResults createGroupedResults() {
        List<GroupedTestSuites> groupedTestSuites = testGroups.collect {
            File junitXmlResultsDirectory = it.task.reports.junitXml.destination

            String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectory(junitXmlResultsDirectory)

            new GroupedTestSuites(
                    groupName: it.projectName,
                    groupLabel: it.taskName,
                    testSuitesBlob: resultsBlob
            )
        }

        return new GroupedResults(groupedTestSuites: groupedTestSuites)
    }
}
