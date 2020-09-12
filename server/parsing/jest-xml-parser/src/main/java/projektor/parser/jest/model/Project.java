package projektor.parser.jest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    public Metrics metrics;

    @JacksonXmlProperty(isAttribute = true)
    public String name;
}
