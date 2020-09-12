package projektor.parser.jest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.jest.model.Coverage;

import java.io.IOException;

public class JestXmlReportParser {
    private final ObjectMapper mapper = new XmlMapper();

    public static boolean isJestReport(String reportXml) {
        return reportXml.contains("clover=");
    }

    public Coverage parseReport(String reportXml) throws IOException {
        Coverage report = mapper.readValue(reportXml, Coverage.class);

        return report;
    }
}
