package projektor.plugin.client

import com.fasterxml.jackson.databind.ObjectMapper

class PayloadSerializer {
    private final ObjectMapper mapper = new ObjectMapper()

    String serializePayload(Object payload) {
        return mapper.writeValueAsString(payload)
    }
}
