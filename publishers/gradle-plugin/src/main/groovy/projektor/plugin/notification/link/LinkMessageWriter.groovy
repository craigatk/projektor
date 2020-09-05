package projektor.plugin.notification.link

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import projektor.plugin.notification.NotificationConfig

class LinkMessageWriter {
    final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    File writeLinkFile(
            LinkModel linkModel,
            NotificationConfig notificationConfig,
            File destinationDirectory
    ) {
        String fileText = objectMapper.writeValueAsString(linkModel)

        File linkFile = new File(destinationDirectory, notificationConfig.linkFileName)
        linkFile.text = fileText

        return linkFile
    }
}
