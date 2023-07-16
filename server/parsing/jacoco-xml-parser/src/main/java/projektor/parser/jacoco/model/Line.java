package projektor.parser.jacoco.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Line {
    @JacksonXmlProperty(isAttribute = true, localName = "nr")
    public Integer number;

    @JacksonXmlProperty(isAttribute = true, localName = "mi")
    public Integer missedInstructions;

    @JacksonXmlProperty(isAttribute = true, localName = "ci")
    public Integer coveredInstructions;

    @JacksonXmlProperty(isAttribute = true, localName = "mb")
    public Integer missedBranches;

    @JacksonXmlProperty(isAttribute = true, localName = "cb")
    public Integer coveredBranches;

    public LineType lineType() {
        if (missedInstructions > 0 && coveredInstructions == 0) {
            return LineType.MISSED;
        } else if (missedBranches > 0) {
            return LineType.PARTIAL;
        }

        return LineType.COVERED;
    }
}
