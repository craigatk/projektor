package projektor.plugin.results.grouped

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper

class GroupedResultsSerializer {
    private final ObjectMapper mapper = new XmlMapper()

    String serializeGroupedResults(GroupedResults groupedResults) {
        return mapper.writeValueAsString(groupedResults)
    }
}
