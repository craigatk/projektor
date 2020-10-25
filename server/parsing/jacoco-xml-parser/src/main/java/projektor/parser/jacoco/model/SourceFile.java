package projektor.parser.jacoco.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceFile {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JsonProperty("line")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Line> lines;

    @JsonProperty("counter")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Counter> counters;
}
