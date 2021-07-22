package projektor.parser.coverage.cobertura.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class CoverageLine {
    public Integer number;

    public Integer hits;

    public Boolean branch;

    @JacksonXmlProperty(isAttribute = true, localName = "condition-coverage")
    public String conditionCoverage;

    @JsonIgnore
    public Boolean isPartial() {
        return branch != null && branch && conditionCoverage != null && !conditionCoverage.contains("100%");
    }

    @JsonIgnore
    public Boolean isCovered() {
        return hits > 0;
    }
}
