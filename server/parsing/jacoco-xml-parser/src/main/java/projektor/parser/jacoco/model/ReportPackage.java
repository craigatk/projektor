package projektor.parser.jacoco.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportPackage {
    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JsonProperty("sourcefile")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<SourceFile> sourceFiles;
}
