package projektor.parser.grouped.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;

public class GroupedTestSuites {
    public String groupName;
    public String groupLabel;
    public String directory;

    @JacksonXmlCData
    public String testSuitesBlob;
}
