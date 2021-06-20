package projektor.parser.coverage.cobertura.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Packages {
    @JacksonXmlProperty(localName = "package")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Pkg> packages;
}
