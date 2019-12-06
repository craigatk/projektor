package projektor.parser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Failure {
    @JacksonXmlProperty(isAttribute = true)
    public String message;

    @JacksonXmlProperty(isAttribute = true)
    public String type;

    @JacksonXmlText(value = true)
    public String text;
}