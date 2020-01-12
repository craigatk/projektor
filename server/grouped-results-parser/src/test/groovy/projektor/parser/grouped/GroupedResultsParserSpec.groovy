package projektor.parser.grouped

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import projektor.parser.grouped.model.GroupedResults
import projektor.parser.grouped.model.GroupedTestSuites
import spock.lang.Specification
import spock.lang.Subject

class GroupedResultsParserSpec extends Specification {
    @Subject
    GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    private final ObjectMapper mapper = new XmlMapper();

    def "should deserialize grouped results from a string"() {
        given:
        String groupedResultsXml = """<GroupedResults><groupedTestSuites><groupName>MyGroup</groupName><groupLabel>MyProject</groupLabel><directory>path/to/my-project</directory><testSuitesBlob><![CDATA[<testsuites>Yes</testsuites>]]></testSuitesBlob></groupedTestSuites></GroupedResults>"""

        when:
        GroupedResults groupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        then:
        groupedResults.groupedTestSuites.size() == 1

        GroupedTestSuites groupedTestSuites = groupedResults.groupedTestSuites[0]
        groupedTestSuites.groupLabel == "MyProject"
        groupedTestSuites.groupName == "MyGroup"
        groupedTestSuites.directory == "path/to/my-project"
        groupedTestSuites.testSuitesBlob == """<testsuites>Yes</testsuites>"""
    }

    def "should deserialize grouped results that were serialized"() {
        given:
        List<GroupedTestSuites> groupedTestSuites = (1..3).collect { idx ->
            new GroupedTestSuites(
                    groupName: "MyGroup${idx}",
                    groupLabel: "MyLabel${idx}",
                    directory: "path/to/my-group-${idx}",
                    testSuitesBlob: """<testsuites>Blob${idx}</testsuites>"""
            )
        }

        GroupedResults groupedResults = new GroupedResults(groupedTestSuites: groupedTestSuites)

        when:
        String groupedResultsXml = mapper.writeValueAsString(groupedResults)
        GroupedResults parsedGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        then:
        parsedGroupedResults.groupedTestSuites.size() == 3

        GroupedTestSuites groupedTestSuites1 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup1" }
        groupedTestSuites1.groupName == "MyGroup1"
        groupedTestSuites1.groupLabel == "MyLabel1"
        groupedTestSuites1.directory == "path/to/my-group-1"
        groupedTestSuites1.testSuitesBlob == """<testsuites>Blob1</testsuites>"""

        GroupedTestSuites groupedTestSuites2 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup2" }
        groupedTestSuites2.groupName == "MyGroup2"
        groupedTestSuites2.groupLabel == "MyLabel2"
        groupedTestSuites2.directory == "path/to/my-group-2"
        groupedTestSuites2.testSuitesBlob == """<testsuites>Blob2</testsuites>"""

        GroupedTestSuites groupedTestSuites3 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup3" }
        groupedTestSuites3.groupName == "MyGroup3"
        groupedTestSuites3.groupLabel == "MyLabel3"
        groupedTestSuites3.directory == "path/to/my-group-3"
        groupedTestSuites3.testSuitesBlob == """<testsuites>Blob3</testsuites>"""
    }
}
