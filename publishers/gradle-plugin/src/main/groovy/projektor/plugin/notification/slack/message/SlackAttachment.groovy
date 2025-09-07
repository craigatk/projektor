package projektor.plugin.notification.slack.message

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
