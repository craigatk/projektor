package projektor.parser.coverage.clover.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Metrics {

    @JacksonXmlProperty(isAttribute = true)
    public Integer statements;

    @JacksonXmlProperty(isAttribute = true, localName = "coveredstatements")
    public Integer coveredStatements;

    @JacksonXmlProperty(isAttribute = true)
    public Integer conditionals;

    @JacksonXmlProperty(isAttribute = true, localName = "coveredconditionals")
    public Integer coveredConditionals;
}
