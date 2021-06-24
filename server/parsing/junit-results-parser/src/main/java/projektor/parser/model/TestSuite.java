package projektor.parser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestSuite {
    public String name;
    public int tests;
    public int skipped;
    public int failures;
    public int errors;
    public LocalDateTime timestamp;
    public String hostname;
    public BigDecimal time;

    public String file; // Set by Cypress on the root suite

    @JsonProperty("system-out")
    public String systemOut;

    @JsonProperty("system-err")
    public String systemErr;

    @JsonProperty("testcase")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<TestCase> testCases;

    @JsonIgnore
    public int getPassingCount() {
        return tests - skipped - failures - errors;
    }
}
