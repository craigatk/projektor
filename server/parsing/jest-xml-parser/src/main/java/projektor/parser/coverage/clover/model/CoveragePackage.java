package projektor.parser.coverage.clover.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoveragePackage {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JsonProperty("file")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<CoverageFile> files;
}
