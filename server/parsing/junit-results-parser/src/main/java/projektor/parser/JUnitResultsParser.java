package projektor.parser;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import projektor.parser.model.TestSuite;
import projektor.parser.model.TestSuites;
import projektor.parser.model.TestSuitesWrapper;

import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.util.List;

public class JUnitResultsParser {
    private final ObjectMapper mapper;

    public JUnitResultsParser() {
        XMLInputFactory xmlInputFactory = new WstxInputFactory();
        xmlInputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, Integer.MAX_VALUE);

        this.mapper = new XmlMapper(xmlInputFactory)
                .registerModule(new JavaTimeModule());
    }

    /**
     * Parses a test suite XML in this format
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuite>
     * </testsuite>
     *
     * @param testSuiteXml
     * @return
     * @throws IOException
     */
    public TestSuite parseTestSuite(String testSuiteXml) throws IOException {
        TestSuite testSuite = mapper.readValue(testSuiteXml, TestSuite.class);

        return testSuite;
    }

    /**
     * Parses a list of test suites in this format
     *
     * <?xml version="1.0" encoding="UTF-8"?>
     * <testsuites>
     *   <testsuite>
     *   </testsuite>
     * </testsuites>
     *
     * @param testSuitesXml
     * @return
     * @throws IOException
     */
    public List<TestSuite> parseTestSuites(String testSuitesXml) throws IOException {
        TestSuites testSuites = mapper.readValue(testSuitesXml, TestSuites.class);

        return testSuites.testSuites;
    }

    /**
     * Parses a synthetic wrapper around a list of <testsuites></testsuites>
     *
     * <testsuiteswrapper>
     *     <testsuites>
     *         <testsuite></testsuite>
     *         <testsuite></testsuite>
     *     </testsuites>
     *     <testsuites>
     *         <testsuite></testsuite>
     *         <testsuite></testsuite>
     *     </testsuites>
     * </testsuiteswrapper>
     *
     * @param testSuitesWrapperXml
     * @return
     * @throws IOException
     */
    public TestSuitesWrapper parseTestSuitesWrapper(String testSuitesWrapperXml) throws IOException {
        return mapper.readValue(testSuitesWrapperXml, TestSuitesWrapper.class);
    }
}
