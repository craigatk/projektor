package projektor.plugin.results.grouped;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

class GroupedResults {
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<GroupedTestSuites> groupedTestSuites
}
