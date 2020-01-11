package projektor.parser.grouped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.grouped.model.GroupedResults;

import java.io.IOException;

public class GroupedResultsParser {
    private final ObjectMapper mapper = new XmlMapper();

    public GroupedResults parseGroupedResults(String groupedResultsXml) throws IOException {
        GroupedResults groupedResults = mapper.readValue(groupedResultsXml, GroupedResults.class);

        return groupedResults;
    }

    public String serializeGroupedResults(GroupedResults groupedResults) throws JsonProcessingException {
        String groupedResultsXml = mapper.writeValueAsString(groupedResults);

        return groupedResultsXml;
    }
}
