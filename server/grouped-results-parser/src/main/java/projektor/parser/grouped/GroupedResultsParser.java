package projektor.parser.grouped;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.grouped.model.GroupedResults;

import java.io.IOException;

public class GroupedResultsParser {
    private final ObjectMapper mapper = new XmlMapper();

    public GroupedResults parseGroupedResults(String groupedResultsXml) throws IOException {
        return mapper.readValue(groupedResultsXml, GroupedResults.class);
    }
}
