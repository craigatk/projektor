package projektor.plugin.results.grouped

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData

class GroupedTestSuites {
    String groupName
    String groupLabel
    String directory

    @JacksonXmlCData
    String testSuitesBlob
}
