package projektor.parser.jest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverageFile {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    public Metrics metrics;

    @JsonProperty("line")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<CoverageLine> lines;
}
