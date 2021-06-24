package projektor.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class TestSuites {
    @JsonProperty("testsuite")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<TestSuite> testSuites;
}
