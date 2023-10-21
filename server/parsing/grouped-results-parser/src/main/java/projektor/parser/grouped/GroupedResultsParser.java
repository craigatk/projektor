package projektor.parser.grouped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import projektor.parser.grouped.model.GroupedResults;

import java.io.IOException;

public class GroupedResultsParser {
    private static final int MAX_PAYLOAD_STRING_LENGTH = 50_000_000; // default is 20_000_000

    private final ObjectMapper mapper;

    public GroupedResultsParser() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.mapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(MAX_PAYLOAD_STRING_LENGTH).build());
    }

    public GroupedResults parseGroupedResults(String groupedResultsBlob) throws IOException {
        return mapper.readValue(groupedResultsBlob, GroupedResults.class);
    }

    public String serializeGroupedResults(GroupedResults groupedResults) throws JsonProcessingException {
        return mapper.writeValueAsString(groupedResults);
    }
}
