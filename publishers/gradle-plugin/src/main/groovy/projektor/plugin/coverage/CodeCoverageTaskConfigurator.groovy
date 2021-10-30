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

                    // Report.enabled is going to be replaced with Report.required in Gradle 8.0,
                    // so use Report.required if it is available
                    if (task.reports.xml.hasProperty("required")) {
                        task.reports.xml.required = true
                    } else {
                        task.reports.xml.enabled = true
                    }

                }
            }
        }
    }
}
