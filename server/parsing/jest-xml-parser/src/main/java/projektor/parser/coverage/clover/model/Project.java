package projektor.parser.coverage.clover.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    public Metrics metrics;

    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JsonProperty("package")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<CoveragePackage> packages;

    // Used if there are no packages
    @JsonProperty("file")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<CoverageFile> files;
}
