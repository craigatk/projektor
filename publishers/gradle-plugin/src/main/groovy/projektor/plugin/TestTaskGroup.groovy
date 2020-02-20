package projektor.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class TestTaskGroup implements TestGroup {
    private final Test task
    private final String taskName
    private final String projectName

    TestTaskGroup(Test task, String taskName, String projectName) {
        this.task = task
        this.taskName = taskName
        this.projectName = projectName
    }

    @Override
    File getResultsDir() {
        return task.reports.junitXml.destination
    }

    @Override
    String getName() {
        return this.projectName
    }

    @Override
    String getLabel() {
        return this.taskName
    }

    static TestTaskGroup fromTask(Test task) {
        Project project = task.project
        String projectName = project.name
        String taskName = task.name

        return new TestTaskGroup(task, taskName, projectName)
    }
}
