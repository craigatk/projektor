package projektor.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion
import projektor.plugin.coverage.CodeCoverageTaskConfigurator

class ProjektorPublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        if (GradleVersion.current() < GradleVersion.version(ProjektorPluginVersion.MINIMUM_GRADLE_VERSION)) {
            throw new GradleException("This version of the Projektor Gradle plugin supports Gradle ${ProjektorPluginVersion.MINIMUM_GRADLE_VERSION}+ only. Please upgrade the version of Gradle your project uses.")
        }

        ProjektorPublishPluginExtension extension = project.extensions.create('projektor', ProjektorPublishPluginExtension.class) as ProjektorPublishPluginExtension

        project.afterEvaluate {
            if (extension.serverUrl) {
                ApplyTestResultsBuildListener.conditionallyAddBuildListener(project, extension)

                ApplyTestResultsPublishTask.conditionallyAddPublishTask(project, extension)

                CodeCoverageTaskConfigurator.conditionallyConfigureCodeCoverageReportTask(project, extension)
            } else {
                project.logger.warn("Projektor plugin enabled but no server specified")
            }
        }
    }
}
