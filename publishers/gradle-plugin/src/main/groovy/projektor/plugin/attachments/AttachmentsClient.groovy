package projektor.plugin.attachments

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.gradle.api.logging.Logger
import projektor.plugin.client.ClientConfig

import static projektor.plugin.client.ClientToken.conditionallyAddPublishTokenToRequest

class AttachmentsClient {
    private final OkHttpClient client
    private final ClientConfig config
    private final Logger logger

    AttachmentsClient(OkHttpClient client, ClientConfig clientConfig, Logger logger) {
        this.client = client
        this.config = clientConfig
        this.logger = logger
    }

    void sendAttachmentToServer(String publicId, File attachment) {
        MediaType mediaType = MediaType.get("octet/stream")

        RequestBody body = RequestBody.create(mediaType, attachment.bytes)

        String attachmentUrl = "${config.serverUrl}/run/${publicId}/attachments/${attachment.name}"
        Request.Builder requestBuilder = new Request.Builder()
                .url(attachmentUrl)
                .post(body)
        conditionallyAddPublishTokenToRequest(requestBuilder, config)
        Request request = requestBuilder.build()

        Response response = null

        try {
            response = client.newCall(request).execute()
        } catch (Exception e) {
            logger.error("Error uploading attachment to Projektor ${attachmentUrl}", e)
        }

        if (!response?.successful) {
            String responseCode = response ? "- response code ${response.code()}" : ""
            logger.warn("Failed to upload attachment to Projektor report ${attachmentUrl} ${responseCode}")
        }
    }
}
