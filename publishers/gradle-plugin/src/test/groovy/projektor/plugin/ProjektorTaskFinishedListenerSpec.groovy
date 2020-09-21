package projektor.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ProjektorTaskFinishedListenerSpec extends Specification {
    def "should calculate test duration"() {
        given:
        LocalDateTime startTime = LocalDateTime.of(LocalDate.of(2020, 9, 20), LocalTime.of(12, 25, 30, 0))
        LocalDateTime endTime = LocalDateTime.of(LocalDate.of(2020, 9, 20), LocalTime.of(12, 55, 31, 2000000))

        DateProvider dateProvider = Mock()
        Logger logger = Mock()

        Project project = Mock()
        Test task = Mock()
        TaskState taskState = Mock()

        ProjektorTaskFinishedListener taskFinishedListener = new ProjektorTaskFinishedListener(dateProvider, logger)

        when:
        taskFinishedListener.beforeExecute(task)
        taskFinishedListener.afterExecute(task, taskState)

        then:
        2 * dateProvider.now() >>> [startTime, endTime]
        1 * taskState.skipped >> false
        1 * taskState.upToDate >> false
        task.project >> project

        and:
        taskFinishedListener.testWallClockDurationInSeconds == 1801.002
    }
}
