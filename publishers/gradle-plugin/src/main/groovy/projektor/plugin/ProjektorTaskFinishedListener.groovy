package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test

class ProjektorTaskFinishedListener implements TaskExecutionListener {
    List<TestTaskGroup> testGroups = []

    private final Logger logger

    ProjektorTaskFinishedListener(Logger logger) {
        this.logger = logger
    }

    @Override
    void beforeExecute(Task task) { }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if (!taskState.skipped && !taskState.upToDate && task instanceof Test) {
            testGroups.add(TestTaskGroup.fromTask(task))
        }
    }
}
