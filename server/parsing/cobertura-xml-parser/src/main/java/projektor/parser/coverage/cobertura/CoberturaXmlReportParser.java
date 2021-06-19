package projektor.parser.coverage.cobertura;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import projektor.parser.coverage.cobertura.model.Coverage;

import java.io.IOException;

public class CoberturaXmlReportParser {
    private final ObjectMapper mapper = new XmlMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static boolean isCoberturaReport(String reportXml) {
        return reportXml.contains("cobertura.sourceforge.net");
    }

    public Coverage parseReport(String reportXml) throws IOException  {
        Coverage coverage = mapper.readValue(reportXml, Coverage.class);

        return coverage;
    }
}
