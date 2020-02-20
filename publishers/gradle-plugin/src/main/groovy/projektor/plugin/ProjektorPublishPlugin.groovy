package projektor.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import projektor.plugin.results.ResultsClientConfig

class ProjektorPublishPlugin implements Plugin<Project> {
    private static final String LISTENER_APPLIED_PROPERTY_NAME = "projektorListenerApplied"

    private static final String PUBLISH_TASK_NAME = "publishResults"

    void apply(Project project) {
        ProjektorPublishPluginExtension extension = project.extensions.create('projektor', ProjektorPublishPluginExtension.class) as ProjektorPublishPluginExtension

        project.afterEvaluate {
            conditionallyAddBuildListener(project, extension)

            conditionallyAddPublishTask(project, extension)
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
                new ResultsClientConfig(extension.serverUrl, Optional.ofNullable(extension.publishToken)),
                logger,
                extension.autoPublishOnFailureOnly,
                project.projectDir,
                extension.additionalResultsDirs,
                projektorTaskFinishedListener
        )
        project.gradle.addBuildListener(projektorBuildFinishedListener)
    }

    static void conditionallyAddPublishTask(Project project, ProjektorPublishPluginExtension extension) {
        if (extension.manualPublishEnabled && extension.serverUrl) {
            project.allprojects.each { proj ->
                if (!proj.tasks.findByPath(PUBLISH_TASK_NAME)) {
                    proj.tasks.create(PUBLISH_TASK_NAME, ProjektorManualPublishTask, { task ->
                        task.serverUrl = extension.serverUrl
                        task.publishToken = extension.publishToken
                        task.additionalResultsDirs = extension.additionalResultsDirs
                    })
                }
            }
        }
    }
}