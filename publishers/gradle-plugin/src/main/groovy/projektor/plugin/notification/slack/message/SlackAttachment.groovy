package projektor.plugin.notification.slack.message

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class SlackAttachment {
    String fallback
    String color
    String pretext
    String title
    String titleLink
    String text
    String footer
    Long ts
}
