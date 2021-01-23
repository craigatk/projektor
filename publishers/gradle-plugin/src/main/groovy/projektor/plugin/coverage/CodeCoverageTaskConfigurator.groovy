package projektor.plugin.coverage

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testing.jacoco.tasks.JacocoReport
import projektor.plugin.ProjektorPublishPluginExtension

class CodeCoverageTaskConfigurator {
    static void conditionallyConfigureCodeCoverageReportTask(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        if (extension.codeCoveragePublish) {
            project.tasks.withType(JacocoReport).each { task ->
                logger.info("Projektor enabling XML report for task ${task.name} in project ${project.name}")
                task.reports.xml.enabled = true
            }
        }
    }
}
