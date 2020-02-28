package projektor.plugin.client

import okhttp3.Request

class ClientToken {
    static final String PUBLISH_TOKEN_NAME = "X-PROJEKTOR-TOKEN"

    static void conditionallyAddPublishTokenToRequest(Request.Builder requestBuilder, ClientConfig config) {
        config.maybePublishToken.ifPresent( { publishToken -> requestBuilder.header(PUBLISH_TOKEN_NAME, publishToken)})
    }
}
