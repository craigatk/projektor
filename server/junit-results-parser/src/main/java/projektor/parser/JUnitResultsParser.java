package projektor.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import projektor.parser.model.TestSuite;
import projektor.parser.model.TestSuites;

import java.io.IOException;
import java.util.List;

public class JUnitResultsParser {
    private final ObjectMapper mapper = new XmlMapper()
            .registerModule(new JavaTimeModule());

    public TestSuite parseResults(String results) throws IOException {
        TestSuite testSuite = mapper.readValue(results, TestSuite.class);

        return testSuite;
    }

    public List<TestSuite> parseResultsGroup(String resultsGroup) throws IOException {
        TestSuites testSuites = mapper.readValue(resultsGroup, TestSuites.class);

        return testSuites.testSuites;
    }
}
