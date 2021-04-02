package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.Test
import projektor.plugin.results.ProjektorResultsCollector
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedTestSuites

class ProjectTestResultsCollector {
    private final Collection<TestGroup> testGroups
    private final Logger logger
    private final ProjektorResultsCollector resultsCollector

    ProjectTestResultsCollector(Collection<TestGroup> testGroups, Logger logger) {
        this.testGroups = testGroups
        this.logger = logger
        this.resultsCollector = new ProjektorResultsCollector(logger)
    }

    static ProjectTestResultsCollector fromAllTasks(
            Collection<Task> allTasks,
            File projectDir,
            List<String> additionalResultsDirs,
            Logger logger
    ) {
        Collection<Test> allTestTasks = allTasks.findAll { it instanceof Test } as Collection<Test>
        Collection<TestTaskGroup> testGroups = allTestTasks.collect { TestTaskGroup.fromTask(it) }
        Collection<TestDirectoryGroup> additionalTestGroups = TestDirectoryGroup.listFromDirPaths(
                projectDir,
                additionalResultsDirs
        )
        Collection<TestGroup> allTestGroups = (testGroups + additionalTestGroups) as Collection<TestGroup>

        return new ProjectTestResultsCollector(allTestGroups, logger)
    }

    boolean hasTestGroups() {
        !testGroups.empty
    }

    int testGroupsCount() {
        testGroups.size()
    }

    GroupedResults createGroupedResults() {
        ProjektorResultsCollector collector = this.resultsCollector

        List<GroupedTestSuites> groupedTestSuites = this.testGroups.collect {
            File junitXmlResultsDirectory = it.getResultsDir()

            String resultsBlob = collector.createResultsBlobFromJunitXmlResultsInDirectory(junitXmlResultsDirectory)

            new GroupedTestSuites(
                    groupName: it.getName(),
                    groupLabel: it.getLabel(),
                    testSuitesBlob: resultsBlob
            )
        }

        return new GroupedResults(groupedTestSuites: groupedTestSuites)
    }
}
