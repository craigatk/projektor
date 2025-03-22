package projektor.parser.grouped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import projektor.parser.grouped.model.GroupedResults;

import java.io.IOException;

public class GroupedResultsParser {
    public static final int DEFAULT_MAX_PAYLOAD_SIZE = 50_000_000; // Jackson default is 20_000_000

    private final ObjectMapper mapper;

    public GroupedResultsParser() {
        this(DEFAULT_MAX_PAYLOAD_SIZE);
    }

    public GroupedResultsParser(Integer maxPayloadSize) {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());

        this.mapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(maxPayloadSize).build());
    }

    public GroupedResults parseGroupedResults(String groupedResultsBlob) throws IOException {
        return mapper.readValue(groupedResultsBlob, GroupedResults.class);
    }

    public String serializeGroupedResults(GroupedResults groupedResults) throws JsonProcessingException {
        return mapper.writeValueAsString(groupedResults);
    }
}
