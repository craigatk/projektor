package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.util.getOrFail
import projektor.notification.slack.InvalidSlackChannelException
import projektor.notification.slack.RepositorySlackChannelService
import projektor.notification.slack.SlackNotificationConfig
import projektor.server.api.repository.RepositorySlackChannel
import projektor.server.api.repository.SaveRepositorySlackChannelError

fun Route.repositorySlack(
    slackNotificationConfig: SlackNotificationConfig,
    repositorySlackChannelService: RepositorySlackChannelService,
) {
    suspend fun handleFetchChannel(
        call: ApplicationCall,
        projectName: String?,
    ) {
        val fullRepoName = "${call.parameters.getOrFail("orgPart")}/${call.parameters.getOrFail("repoPart")}"

        if (!slackNotificationConfig.enabled) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            repositorySlackChannelService.fetchChannelName(fullRepoName, projectName)
                ?.let { call.respond(HttpStatusCode.OK, RepositorySlackChannel(it)) }
                ?: call.respond(HttpStatusCode.NoContent)
        }
    }

    suspend fun handleSaveChannel(
        call: ApplicationCall,
        projectName: String?,
    ) {
        val fullRepoName = "${call.parameters.getOrFail("orgPart")}/${call.parameters.getOrFail("repoPart")}"

        if (!slackNotificationConfig.enabled) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            try {
                val channelName = call.receive<RepositorySlackChannel>().channelName
                val savedChannelName = repositorySlackChannelService.saveChannelName(fullRepoName, projectName, channelName)

                call.respond(HttpStatusCode.OK, RepositorySlackChannel(savedChannelName))
            } catch (e: InvalidSlackChannelException) {
                call.respond(HttpStatusCode.BadRequest, SaveRepositorySlackChannelError(e.message ?: "Invalid Slack channel"))
            }
        }
    }

    suspend fun handleDeleteChannel(
        call: ApplicationCall,
        projectName: String?,
    ) {
        val fullRepoName = "${call.parameters.getOrFail("orgPart")}/${call.parameters.getOrFail("repoPart")}"

        if (!slackNotificationConfig.enabled) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            repositorySlackChannelService.deleteChannelName(fullRepoName, projectName)

            call.respond(HttpStatusCode.OK)
        }
    }

    get("/repo/{orgPart}/{repoPart}/notification/slack") {
        handleFetchChannel(call, null)
    }

    get("/repo/{orgPart}/{repoPart}/project/{projectName}/notification/slack") {
        handleFetchChannel(call, call.parameters.getOrFail("projectName"))
    }

    put("/repo/{orgPart}/{repoPart}/notification/slack") {
        handleSaveChannel(call, null)
    }

    put("/repo/{orgPart}/{repoPart}/project/{projectName}/notification/slack") {
        handleSaveChannel(call, call.parameters.getOrFail("projectName"))
    }

    delete("/repo/{orgPart}/{repoPart}/notification/slack") {
        handleDeleteChannel(call, null)
    }

    delete("/repo/{orgPart}/{repoPart}/project/{projectName}/notification/slack") {
        handleDeleteChannel(call, call.parameters.getOrFail("projectName"))
    }
}
