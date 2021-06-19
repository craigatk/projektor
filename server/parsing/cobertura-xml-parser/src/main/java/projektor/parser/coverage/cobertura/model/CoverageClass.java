package projektor.parser.coverage.cobertura.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CoverageClass {

    public String name;

    @JacksonXmlProperty(isAttribute = true, localName = "filename")
    public String fileName;

    public Lines lines;
}
