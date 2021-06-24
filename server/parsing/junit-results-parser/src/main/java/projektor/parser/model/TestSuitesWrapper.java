package projektor.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class TestSuitesWrapper {
    @JsonProperty("testsuites")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<TestSuites> testSuites;
}
