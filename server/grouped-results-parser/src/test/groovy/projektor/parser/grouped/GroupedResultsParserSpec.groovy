package projektor.parser.grouped

import com.fasterxml.jackson.databind.ObjectMapper
import projektor.parser.grouped.model.GroupedResults
import projektor.parser.grouped.model.GroupedTestSuites
import spock.lang.Specification
import spock.lang.Subject

class GroupedResultsParserSpec extends Specification {
    @Subject
    GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    private final ObjectMapper mapper = new ObjectMapper()

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
