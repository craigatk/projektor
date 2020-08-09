package projektor.parser.jacoco.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Counter {
    @JacksonXmlProperty(isAttribute = true)
    public String type;

    @JacksonXmlProperty(isAttribute = true)
    public Integer missed;

    @JacksonXmlProperty(isAttribute = true)
    public Integer covered;
}
