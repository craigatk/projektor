package projektor.plugin.coverage

import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
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
        return collectJacocoCodeCoverageFiles(coverageEnabled, allTasks) + collectKoverCodeCoverageFiles(coverageEnabled, allTasks)
    }

    private List<CodeCoverageFile> collectKoverCodeCoverageFiles(boolean coverageEnabled, Collection<Task> allTasks) {
        if (coverageEnabled) {
            Collection<Task> koverXmlReportTasks = allTasks.findAll { it.name.contains("koverXmlReport") }
            return koverXmlReportTasks.collect { koverCoverageFileOrNull(it) }.findAll { it != null }
        } else {
            return []
        }
    }

    private CodeCoverageFile koverCoverageFileOrNull(Task koverXmlReportTask) {
        if (!koverXmlReportTask.xmlReportFile.isPresent() || !koverXmlReportTask.outputDirs.isPresent()) {
            logger.info("Unable to set Projektor Kover coverage: Found no source files or source directories.")
            return null
        } else {
            File reportFile = koverXmlReportTask.xmlReportFile.get().getAsFile()
            String baseDirectoryPath = findKoverBaseDirectoryPath(koverXmlReportTask)

            return new CodeCoverageFile(
                    reportFile,
                    baseDirectoryPath
            )
        }
    }

    private List<CodeCoverageFile> collectJacocoCodeCoverageFiles(boolean coverageEnabled, Collection<Task> allTasks) {
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
            String baseDirectoryPath = findJacocoBaseDirectoryPath(reportTask)

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

    private String findJacocoBaseDirectoryPath(JacocoReport reportTask) {
        List<File> sourceDirectoriesWithFiles = reportTask.sourceDirectories.findAll { it.list() }
        File projectRootDir = reportTask.project.rootDir

        return findBaseDirectoryPath(sourceDirectoriesWithFiles, projectRootDir)
    }

    private String findKoverBaseDirectoryPath(Task koverReportTask) {
        Property<FileCollection> srcDirsProperty = koverReportTask.srcDirs
        List<File> sourceDirectoriesWithFiles = srcDirsProperty.get().files.toList()
        File projectRootDir = koverReportTask.project.rootDir

        return findBaseDirectoryPath(sourceDirectoriesWithFiles, projectRootDir)
    }

    private String findBaseDirectoryPath(List<File> sourceDirectoriesWithFiles, File projectRootDir) {
        if (sourceDirectoriesWithFiles.size() == 1) {
            return DirectoryUtil.findSubDirectoryPath(projectRootDir, sourceDirectoriesWithFiles.first())
        } else if (sourceDirectoriesWithFiles.size() > 1) {
            logger.info("Unable to set Projektor coverage base directory path: Found multiple source directories containing source files", sourceDirectoriesWithFiles)

            return null
        } else {
            logger.info("Unable to set Projektor coverage base directory path: Found no source directories containing source files")

            return null
        }
    }
}
