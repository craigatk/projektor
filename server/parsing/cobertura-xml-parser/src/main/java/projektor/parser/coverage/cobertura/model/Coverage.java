package projektor.parser.coverage.cobertura.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;

public class Coverage {
    @JacksonXmlProperty(isAttribute = true, localName = "lines-valid")
    public Integer linesValid;

    @JacksonXmlProperty(isAttribute = true, localName = "lines-covered")
    public Integer linesCovered;

    @JacksonXmlProperty(isAttribute = true, localName = "line-rate")
    public BigDecimal lineRate;

    @JacksonXmlProperty(isAttribute = true, localName = "branches-valid")
    public Integer branchesValid;

    @JacksonXmlProperty(isAttribute = true, localName = "branches-covered")
    public Integer branchesCovered;

    @JacksonXmlProperty(isAttribute = true, localName = "branch-rate")
    public BigDecimal branchRate;

    public Packages packages;
}
