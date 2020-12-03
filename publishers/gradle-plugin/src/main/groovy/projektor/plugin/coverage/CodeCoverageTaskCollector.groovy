package projektor.plugin.coverage

import org.gradle.BuildResult
import org.gradle.api.logging.Logger
import org.gradle.api.reporting.SingleFileReport
import org.gradle.testing.jacoco.tasks.JacocoReport
import projektor.plugin.file.DirectoryUtil

class CodeCoverageTaskCollector {
    final List<CodeCoverageFile> codeCoverageFiles

    private final Logger logger
    private final BuildResult buildResult

    CodeCoverageTaskCollector(BuildResult buildResult, boolean coverageEnabled, Logger logger) {
        this.buildResult = buildResult
        this.logger = logger

        if (coverageEnabled) {
            List<JacocoReport> jacocoTasks = buildResult.gradle.taskGraph.allTasks.findAll {
                it instanceof JacocoReport
            } as List<JacocoReport>

            this.codeCoverageFiles = jacocoTasks.collect { coverageFileOrNull(it) }.findAll { it != null }
        } else {
            this.codeCoverageFiles = []
        }
    }

    boolean hasCodeCoverageData() {
        return !codeCoverageFiles.empty
    }

    private CodeCoverageFile coverageFileOrNull(JacocoReport reportTask) {
        SingleFileReport xmlReport = reportTask.reports.xml

        File xmlReportFile = xmlReport.destination

        if (xmlReportFile.exists()) {
            String baseDirectoryPath = findBaseDirectoryPath(reportTask)

            logger.info("Projektor found XML code coverage report from task ${reportTask.name} in project ${reportTask.project.name} with base directory path ${baseDirectoryPath}")

            return new CodeCoverageFile(xmlReportFile, baseDirectoryPath)
        } else {
            logger.info("Projektor found no XML report for Jacoco task ${reportTask.name} in project ${reportTask.project.name}")

            return null
        }
    }

    private String findBaseDirectoryPath(JacocoReport reportTask) {
        List<File> sourceDirectoriesWithFiles = reportTask.sourceDirectories.findAll { it.list() }

        if (sourceDirectoriesWithFiles.size() == 1) {
            return DirectoryUtil.findSubDirectoryPath(reportTask.project.rootDir, sourceDirectoriesWithFiles.first())
        } else if (sourceDirectoriesWithFiles.size() > 1) {
            logger.info("Unable to set Projektor coverage base directory path: Found multiple source directories containing source files", sourceDirectoriesWithFiles)

            return null
        } else {
            logger.info("Unable to set Projektor coverage base directory path: Found no source directories containing source files")

            return null
        }
    }
}
