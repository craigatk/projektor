package projektor.parser.coverage.cobertura.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Classes {
    @JacksonXmlProperty(localName = "class")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<CoverageClass> clazz;
}
