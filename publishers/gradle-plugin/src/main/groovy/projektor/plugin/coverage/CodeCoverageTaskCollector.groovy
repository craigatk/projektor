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
        codeCoverageFiles
                .findAll { coverageFile -> coverageFile.reportFile.exists() }
                .collect { coverageFile ->
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
            return koverXmlReportTasks.collect { koverCoverageFiles(it) }.flatten()
        } else {
            return []
        }
    }

    private List<CodeCoverageFile> koverCoverageFiles(Task koverXmlReportTask) {
        if (koverXmlReportTask) {
            if (koverXmlReportTask.hasProperty("reportFile")) {
                if (koverXmlReportTask.outputDirs?.isPresent() && koverXmlReportTask.reportFile.get().getAsFile().exists()) {
                    // Kover prior to 0.6
                    File reportFile = koverXmlReportTask.xmlReportFile.get().getAsFile()
                    String baseDirectoryPath = findKoverBaseDirectoryPath(koverXmlReportTask)

                    CodeCoverageFile coverageFile = new CodeCoverageFile(
                            reportFile,
                            baseDirectoryPath
                    )

                    return [coverageFile]
                }
            } else {
                // Kover 0.6 and later removed the 'xmlReportFile` field, so get the report xml files
                // from the task output
                FileCollection outputFiles = koverXmlReportTask.outputs.getFiles()

                if (!outputFiles.toList().empty) {
                    String baseDirectoryPath = findKoverBaseDirectoryPath(koverXmlReportTask)

                    return outputFiles
                            .findAll { File reportFile -> reportFile.name.endsWith("xml") }
                            .collect { File reportFile ->
                                new CodeCoverageFile(
                                        reportFile,
                                        baseDirectoryPath
                                )
                            }
                } else {
                    logger.info("Unable to set Projektor Kover coverage: Could not find kover XML task")
                }
            }
        } else {
            logger.info("Unable to set Projektor Kover coverage: Could not find kover XML task")
        }

        return []
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
        FileCollection srcDirs

        if (koverReportTask.hasProperty("srcDirs")) {
            Property<FileCollection> srcDirsProperty = koverReportTask.srcDirs
            srcDirs = srcDirsProperty.get()
        } else {
            srcDirs = koverReportTask.inputs.files
        }
        List<File> sourceDirectoriesWithFiles = srcDirs.files.toList()
        File projectRootDir = koverReportTask.project.rootDir

        return findBaseDirectoryPath(sourceDirectoriesWithFiles, projectRootDir, ["/java"])
    }

    private String findBaseDirectoryPath(List<File> sourceDirectoriesWithFiles, File projectRootDir, List<String> sourceDirectoryPatternsToExclude = []) {
        List<File> filteredSourceDirectories = sourceDirectoriesWithFiles
                .findAll { !it.path.contains("src/main/resources") }
                .findAll { !it.path.contains("/build/") }
                .findAll { !it.path.contains("/caches/") }

        if (sourceDirectoryPatternsToExclude.size() > 0) {
            sourceDirectoryPatternsToExclude.each { toExclude ->
                filteredSourceDirectories = filteredSourceDirectories.findAll { !it.path.contains(toExclude) }
            }
        }

        if (filteredSourceDirectories.size() == 1) {
            return DirectoryUtil.findSubDirectoryPath(projectRootDir, filteredSourceDirectories.first())
        } else if (filteredSourceDirectories.size() > 1) {
            logger.info("Unable to set Projektor coverage base directory path: Found multiple source directories containing source files: {}", filteredSourceDirectories)

            return null
        } else {
            logger.info("Unable to set Projektor coverage base directory path: Found no source directories containing source files")

            return null
        }
    }
}
