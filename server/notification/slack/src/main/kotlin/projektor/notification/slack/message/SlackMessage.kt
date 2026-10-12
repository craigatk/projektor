package projektor.notification.slack.message

data class SlackMessage(
    val channel: String,
    val text: String,
    val blocks: List<SlackBlock>? = null,
)

data class SlackBlock(val type: String, val text: SlackText)

data class SlackText(val type: String, val text: String)
