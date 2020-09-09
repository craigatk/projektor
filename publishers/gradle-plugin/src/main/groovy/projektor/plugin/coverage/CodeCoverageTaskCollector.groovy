package projektor.plugin.coverage

import org.gradle.BuildResult
import org.gradle.api.logging.Logger
import org.gradle.api.reporting.SingleFileReport
import org.gradle.testing.jacoco.tasks.JacocoReport

class CodeCoverageTaskCollector {
    final List<CodeCoverageGroup> codeCoverageGroups

    private final Logger logger
    private final BuildResult buildResult

    CodeCoverageTaskCollector(BuildResult buildResult, boolean coverageEnabled, Logger logger) {
        this.buildResult = buildResult
        this.logger = logger

        if (coverageEnabled) {
            List<JacocoReport> jacocoTasks = buildResult.gradle.taskGraph.allTasks.findAll { it instanceof JacocoReport }

            this.codeCoverageGroups = jacocoTasks.collect { coverageGroupOrNull(it) }.findAll { it != null }
        } else {
            this.codeCoverageGroups = []
        }
    }

    boolean hasCodeCoverageData() {
        return !codeCoverageGroups.empty
    }

    private CodeCoverageGroup coverageGroupOrNull(JacocoReport reportTask) {
        SingleFileReport xmlReport = reportTask.reports.xml

        File xmlReportFile = xmlReport.destination

        if (xmlReportFile.exists()) {
            logger.info("Projektor found XML code coverage report from task ${reportTask.name} in project ${reportTask.project.name}")

            return new CodeCoverageGroup(xmlReportFile)
        } else {
            logger.info("Projektor found no XML report for Jacoco task ${reportTask.name} in project ${reportTask.project.name}")

            return null
        }
    }
}
