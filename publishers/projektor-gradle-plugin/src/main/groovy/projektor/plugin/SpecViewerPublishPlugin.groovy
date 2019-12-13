package projektor.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class ProjektorPublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        ProjektorPublishPluginExtension extension = project.extensions.create('projektor', ProjektorPublishPluginExtension.class) as ProjektorPublishPluginExtension

        Logger logger = project.logger

        project.afterEvaluate {
            if (extension.autoPublish) {
                if (extension.serverUrl) {
                    ProjektorTaskFinishedListener projektorTaskFinishedListener = new ProjektorTaskFinishedListener(
                            logger
                    )
                    project.gradle.taskGraph.addTaskExecutionListener(projektorTaskFinishedListener)

                    ProjektorBuildFinishedListener projektorBuildFinishedListener = new ProjektorBuildFinishedListener(
                            extension.serverUrl,
                            logger,
                            extension.autoPublishOnFailureOnly,
                            projektorTaskFinishedListener
                    )
                    project.gradle.addBuildListener(projektorBuildFinishedListener)
                } else {
                    logger.warn("Projektor plugin enabled but no server specified")
                }

            } else {
                logger.info("Projektor plugin auto-publish disabled")
            }

            if (extension.manualPublishEnabled && extension.serverUrl) {
                project.allprojects.each { proj ->
                    proj.tasks.create("publishResults", ProjektorManualPublishTask, { task ->
                        task.serverUrl = extension.serverUrl
                    })
                }
            }
        }
    }
}