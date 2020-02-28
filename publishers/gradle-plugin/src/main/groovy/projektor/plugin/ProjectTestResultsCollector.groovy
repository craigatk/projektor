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

    ProjectTestResultsCollector(Collection<TestTaskGroup> testGroups, Logger logger) {
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

        return new ProjectTestResultsCollector(testGroups + additionalTestGroups, logger)
    }

    boolean hasTestGroups() {
        !testGroups.empty
    }

    int testGroupsCount() {
        testGroups.size()
    }

    GroupedResults createGroupedResults() {
        List<GroupedTestSuites> groupedTestSuites = testGroups.collect {
            File junitXmlResultsDirectory = it.resultsDir

            String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectory(junitXmlResultsDirectory)

            new GroupedTestSuites(
                    groupName: it.name,
                    groupLabel: it.label,
                    testSuitesBlob: resultsBlob
            )
        }

        return new GroupedResults(groupedTestSuites: groupedTestSuites)
    }
}
