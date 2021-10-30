package projektor.plugin.coverage

import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.reporting.SingleFileReport
import org.gradle.testing.jacoco.tasks.JacocoReport
import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.file.DirectoryUtil

class CodeCoverageTaskCollector {
    final List<CodeCoverageFile> codeCoverageFiles

    private final Logger logger

    CodeCoverageTaskCollector(Collection<Task> allTasks, boolean coverageEnabled, Logger logger) {
        this.logger = logger
        this.codeCoverageFiles = collectCodeCoverageFiles(allTasks, coverageEnabled)
    }

    boolean hasCodeCoverageData() {
        return !codeCoverageFiles.empty
    }

    List<CoverageFilePayload> getCoverageFilePayloads() {
        codeCoverageFiles.collect { coverageFile ->
            new CoverageFilePayload(
                    reportContents: coverageFile.reportFile.text,
                    baseDirectoryPath: coverageFile.baseDirectoryPath
            )
        }
    }

    private List<CodeCoverageFile> collectCodeCoverageFiles(Collection<Task> allTasks, boolean coverageEnabled) {
        if (coverageEnabled) {
            List<JacocoReport> jacocoTasks = allTasks.findAll {
                it instanceof JacocoReport
            } as List<JacocoReport>

            return jacocoTasks.collect { coverageFileOrNull(it) }.findAll { it != null }
        } else {
            return []
        }
    }

    private CodeCoverageFile coverageFileOrNull(JacocoReport reportTask) {
        File xmlReportFile = extractReportFile(reportTask)

        if (xmlReportFile.exists()) {
            String baseDirectoryPath = findBaseDirectoryPath(reportTask)

            logger.info("Projektor found XML code coverage report from task ${reportTask.name} in project ${reportTask.project.name} with base directory path ${baseDirectoryPath}")

            return new CodeCoverageFile(xmlReportFile, baseDirectoryPath)
        } else {
            logger.info("Projektor found no XML report for Jacoco task ${reportTask.name} in project ${reportTask.project.name}")

            return null
        }
    }

    private static File extractReportFile(JacocoReport reportTask) {
        SingleFileReport xmlReport = reportTask.reports.xml

        // Report.destination is going to be replaced with Report.outputLocation in Gradle 8.0,
        // so use Report.outputLocation if it is available
        if (xmlReport.hasProperty("outputLocation")) {
            return xmlReport.outputLocation.asFile.get()
        } else {
            return xmlReport.destination
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
