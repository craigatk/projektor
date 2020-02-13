package projektor.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class ProjektorPublishPlugin implements Plugin<Project> {
    private static final String LISTENER_APPLIED_PROPERTY_NAME = "projektorListenerApplied"

    void apply(Project project) {
        ProjektorPublishPluginExtension extension = project.extensions.create('projektor', ProjektorPublishPluginExtension.class) as ProjektorPublishPluginExtension

        project.afterEvaluate {
            conditionallyAddBuildListener(project, extension)

            if (extension.manualPublishEnabled && extension.serverUrl) {
                project.allprojects.each { proj ->
                    proj.tasks.create("publishResults", ProjektorManualPublishTask, { task ->
                        task.serverUrl = extension.serverUrl
                    })
                }
            }
        }
    }

    private static void conditionallyAddBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        if (extension.autoPublish) {
            if (extension.serverUrl) {
                if (!project.gradle.ext.has(LISTENER_APPLIED_PROPERTY_NAME)) {
                    addBuildListener(project, extension)

                    project.gradle.ext.set(LISTENER_APPLIED_PROPERTY_NAME, true)
                } else {
                    logger.info("Projektor build listener already applied, skipping")
                }
            } else {
                logger.warn("Projektor plugin enabled but no server specified")
            }
        } else {
            logger.info("Projektor plugin auto-publish disabled")
        }
    }

    static void addBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

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
    }
}