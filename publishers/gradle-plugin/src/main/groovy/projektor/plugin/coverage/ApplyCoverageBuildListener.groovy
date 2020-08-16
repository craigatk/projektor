package projektor.plugin.coverage

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import projektor.plugin.ProjektorPublishPluginExtension

class ApplyCoverageBuildListener {
    private static final String LISTENER_APPLIED_PROPERTY_NAME = "projektorCoverageListenerApplied"

    static void conditionallyAddBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        if (extension.codeCoveragePublish) {
            if (!project.gradle.ext.has(LISTENER_APPLIED_PROPERTY_NAME)) {
                addBuildListener(project, extension)

                project.gradle.ext.set(LISTENER_APPLIED_PROPERTY_NAME, true)
            } else {
                logger.info("Projektor coverage build listener already applied, skipping")
            }
        } else {
            logger.info("Projektor plugin code coverage collection and publish disabled")
        }
    }

    private static void addBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        CodeCoverageTaskFinishedListener codeCoverageTaskFinishedListener = new CodeCoverageTaskFinishedListener(logger)
        project.gradle.taskGraph.addTaskExecutionListener(codeCoverageTaskFinishedListener)
    }
}
