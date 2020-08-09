package projektor.parser.jacoco.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JsonProperty("counter")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Counter> counters;
}
