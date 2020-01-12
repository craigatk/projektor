package projektor.plugin.results.grouped

import com.fasterxml.jackson.databind.ObjectMapper

class GroupedResultsSerializer {
    private final ObjectMapper mapper = new ObjectMapper()

    String serializeGroupedResults(GroupedResults groupedResults) {
        return mapper.writeValueAsString(groupedResults)
    }
}
