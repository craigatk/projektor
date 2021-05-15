package projektor.parser.coverage.clover.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverageLine {
    //  <line num="22" count="15" type="cond" truecount="1" falsecount="1"/>

    @JacksonXmlProperty(isAttribute = true, localName = "num")
    public Integer number;

    @JacksonXmlProperty(isAttribute = true, localName = "count")
    public Integer count;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    public String lineType;

    @JacksonXmlProperty(isAttribute = true, localName = "truecount")
    public Integer trueCount;

    @JacksonXmlProperty(isAttribute = true, localName = "falsecount")
    public Integer falseCount;

    public LineType lineCoverageType() {
        if (("cond".equals(lineType) || "stmt".equals(lineType)) && count == 0) {
            return LineType.MISSED;
        } else if ("cond".equals(lineType) && (trueCount == 0 || falseCount == 0)) {
            return LineType.PARTIAL;
        }

        return LineType.COVERED;
    }
}
