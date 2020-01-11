package projektor.plugin

import groovy.transform.TupleConstructor
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

@TupleConstructor(allProperties = true)
class TestGroup {
    final Test task
    final String taskName
    final String projectName
    final File projectDir
    final File rootDir

    static TestGroup fromTask(Test task) {
        Project project = task.project
        File projectDir = project.projectDir
        File rootDir = project.rootDir
        String projectName = project.name
        String taskName = task.name

        return new TestGroup(task, taskName, projectName, projectDir, rootDir)
    }
}
