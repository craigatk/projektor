package projektor.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import projektor.plugin.client.ClientConfig
import projektor.plugin.notification.NotificationConfig

class ApplyTestResultsBuildListener {
    private static final String LISTENER_APPLIED_PROPERTY_NAME = "projektorTestListenerApplied"

    static void conditionallyAddBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        if (extension.autoPublish) {
            if (!project.gradle.ext.has(LISTENER_APPLIED_PROPERTY_NAME)) {
                addBuildListener(project, extension)

                project.gradle.ext.set(LISTENER_APPLIED_PROPERTY_NAME, true)
            } else {
                logger.info("Projektor test results build listener already applied, skipping")
            }
        } else {
            logger.info("Projektor plugin auto-publish disabled")
        }
    }

    static void addBuildListener(Project project, ProjektorPublishPluginExtension extension) {
        Logger logger = project.logger

        ProjektorTaskFinishedListener projektorTaskFinishedListener = new ProjektorTaskFinishedListener(
                new DateProvider(),
                logger
        )
        project.gradle.taskGraph.addTaskExecutionListener(projektorTaskFinishedListener)

        ProjektorBuildFinishedListener projektorBuildFinishedListener = new ProjektorBuildFinishedListener(
                new ClientConfig(
                        extension.serverUrl,
                        extension.compressionEnabled,
                        Optional.ofNullable(extension.publishToken),
                        extension.publishRetryMaxAttempts,
                        extension.publishRetryInterval,
                        extension.publishTimeout
                ),
                new NotificationConfig(
                        extension.writeSlackMessageFile,
                        extension.slackMessageFileName,
                        extension.writeLinkFile,
                        extension.linkFileName
                ),
                logger,
                project.projectDir,
                extension,
                projektorTaskFinishedListener
        )
        project.gradle.addBuildListener(projektorBuildFinishedListener)
    }
}
