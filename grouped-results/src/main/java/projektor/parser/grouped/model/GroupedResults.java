package projektor.parser.grouped.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class GroupedResults {
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<GroupedTestSuites> groupedTestSuites;
}
