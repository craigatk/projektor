package projektor.plugin.coverage

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.Logger
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.TaskState
import org.gradle.testing.jacoco.tasks.JacocoReport

class CodeCoverageTaskFinishedListener implements TaskExecutionListener {
    List<CodeCoverageGroup> codeCoverageGroups = []

    private final Logger logger

    CodeCoverageTaskFinishedListener(Logger logger) {
        this.logger = logger
    }

    boolean hasCodeCoverageData() {
        return !codeCoverageGroups.empty
    }

    @Override
    void beforeExecute(Task task) { }

    @Override
    void afterExecute(Task task, TaskState taskState) {
        if (!taskState.skipped && !taskState.upToDate && task instanceof JacocoReport) {
            CodeCoverageGroup codeCoverageGroup = coverageGroupOrNull(task)

            if (codeCoverageGroup != null) {
                codeCoverageGroups.add(codeCoverageGroup)
            }
        }
    }

    private CodeCoverageGroup coverageGroupOrNull(JacocoReport reportTask) {
        SingleFileReport xmlReport = reportTask.reports.xml

        File xmlReportFile = xmlReport.outputLocation.getAsFile().get()

        if (xmlReportFile.exists()) {
            logger.info("Projektor found XML code coverage report from task ${reportTask.name} in project ${reportTask.project.name}")

            return new CodeCoverageGroup(xmlReportFile)
        } else {
            logger.info("Projektor found no XML report for Jacoco task ${reportTask.name} in project ${reportTask.project.name}")

            return null
        }
    }
}
