package projektor.plugin

import org.gradle.api.Project

class ApplyTestResultsPublishTask {
    private static final String PUBLISH_TASK_NAME = "publishResults"

    static void conditionallyAddPublishTask(Project project, ProjektorPublishPluginExtension extension) {
        if (extension.publishTaskEnabled && extension.serverUrl) {
            project.allprojects.each { proj ->
                if (!proj.tasks.findByPath(PUBLISH_TASK_NAME)) {
                    proj.tasks.create(PUBLISH_TASK_NAME, ProjektorManualPublishTask, { task ->
                        task.serverUrl = extension.serverUrl
                        task.publishToken = extension.publishToken
                        task.additionalResultsDirs = extension.additionalResultsDirs
                        task.attachments = extension.attachments
                    })
                }
            }
        }
    }
}
