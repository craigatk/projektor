package projektor.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import projecktor.results.merge.ResultsXmlMerger;
import projektor.parser.model.TestSuite;
import projektor.parser.model.TestSuites;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestResultsParser {
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

    /**
     * Handles parsing results XML docs in a variety of formats, including <testsuites>
     * results docs back-to-back with <xml> declarations in between.
     *
     * For example, handles:
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     *
     * or
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     *
     * or
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuite>
     * </testsuite>
     *
     * @param resultsBlob
     * @return
     * @throws IOException
     */
    public List<TestSuite> parseResultsBlob(String resultsBlob) throws IOException {
        if (resultsBlob != null && resultsBlob.length() > 0) {
            String resultsGroup = ResultsXmlMerger.cleanAndMergeBlob(resultsBlob);

            return parseResultsGroup(resultsGroup);
        } else {
            return Arrays.asList();
        }
    }
}