package projektor.parser.grouped.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;

public class GroupedTestSuites {
    public String projectName;
    public String groupName;
    public String directory;
    public String path;

    @JacksonXmlCData
    public String testSuitesBlob;
}
