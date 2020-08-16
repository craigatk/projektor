package projektor.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import projektor.plugin.coverage.ApplyCoverageBuildListener

class ProjektorPublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        ProjektorPublishPluginExtension extension = project.extensions.create('projektor', ProjektorPublishPluginExtension.class) as ProjektorPublishPluginExtension

        project.afterEvaluate {
            if (extension.serverUrl) {
                ApplyTestResultsBuildListener.conditionallyAddBuildListener(project, extension)

                ApplyTestResultsPublishTask.conditionallyAddPublishTask(project, extension)

                ApplyCoverageBuildListener.conditionallyAddBuildListener(project, extension)
            } else {
                project.logger.warn("Projektor plugin enabled but no server specified")
            }
        }
    }
}
