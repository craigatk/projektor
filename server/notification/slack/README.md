# Slack notifications

Sends a Slack message when a CI run on a repository's mainline branch:

* goes red (the previous mainline run passed, or there was no previous run)
* drops line coverage compared to the previous mainline run
* makes a test flaky (the failure in this run is the one that reaches the flaky threshold)

Each repository (and optionally project within the repository, for mono-repos) is mapped to its own Slack channel.
The mapping is set from the dashboard of any test report that has Git repository information.
A repository and project without a channel mapping doesn't get notifications.

## Slack app setup

1. Create a Slack app in your workspace with the `chat:write` bot token scope and install it to the workspace.
2. Invite the app's bot user to each channel that should receive notifications.
   Projektor posts a confirmation message when a channel is mapped, so a channel the bot can't post to is rejected right away.

## Server configuration

| Environment variable            | Required | Description                                                                               |
|---------------------------------|----------|-------------------------------------------------------------------------------------------|
| `SLACK_API_URL`                 | Yes      | Base URL of the Slack Web API, `https://slack.com/api`                                    |
| `SLACK_BOT_TOKEN`               | Yes      | Bot token of the Slack app (`xoxb-...`)                                                   |
| `SERVER_BASE_URL`               | Yes      | Base URL of the Projektor server, used for the links in the notification                  |
| `SLACK_COVERAGE_DROP_THRESHOLD` | No       | Minimum drop in line coverage percentage points to notify about. Defaults to `0.1`        |
| `SLACK_FLAKY_MAX_RUNS`          | No       | Number of recent mainline runs to look at when finding flaky tests. Defaults to `50`      |
| `SLACK_FLAKY_FAILURE_THRESHOLD` | No       | Number of failures in those runs for a test to be considered flaky. Defaults to `5`       |

Slack notifications and the channel mapping setting in the UI are only enabled when both `SLACK_API_URL` and `SLACK_BOT_TOKEN` are set.
