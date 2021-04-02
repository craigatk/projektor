package projektor.plugin

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test

import java.time.Duration
import java.time.LocalDateTime

class ProjektorTaskFinishedListener implements TaskExecutionListener {
    private List<TestTaskGroup> testGroups = []
    private LocalDateTime testsStarted
    private LocalDateTime testsFinished

    private final DateProvider dateProvider
    private final Logger logger

    ProjektorTaskFinishedListener(DateProvider dateProvider, Logger logger) {
        this.dateProvider = dateProvider
        this.logger = logger
    }

    List<TestTaskGroup> getTestGroups() {
        return this.testGroups
    }

    @Override
    void beforeExecute(Task task) {
        if (testsStarted == null && task instanceof Test) {
            testsStarted = dateProvider.now()
        }
    }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if (!taskState.skipped && !taskState.upToDate && task instanceof Test) {
            testGroups.add(TestTaskGroup.fromTask(task))

            LocalDateTime now = dateProvider.now()
            if (testsFinished == null || testsFinished.isBefore(now)) {
                this.testsFinished = now
            }
        }
    }

    BigDecimal getTestWallClockDurationInSeconds() {
        if (testsStarted && testsFinished) {
            BigDecimal millis = Duration.between(testsStarted, testsFinished).toMillis().toBigDecimal()

            return millis / 1000
        } else {
            return null
        }
    }
}
