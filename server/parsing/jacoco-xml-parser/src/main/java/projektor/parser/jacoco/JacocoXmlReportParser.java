package projektor.parser.jacoco;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.jacoco.model.Report;

import java.io.IOException;

public class JacocoXmlReportParser {
    private final ObjectMapper mapper = new XmlMapper();

    public static boolean isJacocoReport(String reportXml) {
        return reportXml.contains("JACOCO//DTD");
    }

    public Report parseReport(String reportXml) throws IOException {
        Report report = mapper.readValue(reportXml, Report.class);

        return report;
    }
}
