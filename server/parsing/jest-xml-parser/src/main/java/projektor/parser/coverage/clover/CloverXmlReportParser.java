package projektor.parser.coverage.clover;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.coverage.clover.model.Coverage;

import java.io.IOException;

public class CloverXmlReportParser {
    private final ObjectMapper mapper = new XmlMapper();

    public static boolean isCloverReport(String reportXml) {
        return reportXml.contains("clover=");
    }

    public Coverage parseReport(String reportXml) throws IOException {
        Coverage report = mapper.readValue(reportXml, Coverage.class);

        return report;
    }
}
