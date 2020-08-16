package projektor.plugin.coverage

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.testing.jacoco.tasks.JacocoReport
import projektor.plugin.ProjektorPublishPluginExtension

class CodeCoverageTaskConfigurator {
    static void conditionallyConfigureCodeCoverageReportTask(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        if (extension.codeCoveragePublish) {
            Set<Task> jacocoTestReportTasks = project.getTasksByName('jacocoTestReport', true)

            jacocoTestReportTasks.each { task ->
                if (task instanceof JacocoReport) {
                    logger.info("Projektor enabling XML report for task ${task.name} in project ${project.name}")
                    task.reports.xml.enabled = true
                }
            }
        }
    }
}
