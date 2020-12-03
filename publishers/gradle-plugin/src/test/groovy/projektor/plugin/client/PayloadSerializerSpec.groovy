package projektor.plugin.client

import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.grouped.model.GroupedResults as ServerGroupedResults
import projektor.parser.grouped.model.GroupedTestSuites as ServerGroupedTestSuites
import projektor.plugin.results.grouped.GroupedResults
import projektor.plugin.results.grouped.GroupedTestSuites
import spock.lang.Specification
import spock.lang.Subject

class PayloadSerializerSpec extends Specification {
    @Subject
    PayloadSerializer payloadSerializer = new PayloadSerializer()

    private GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    def "should be able to deserialize grouped results that were serialized"() {
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
        String groupedResultsXml = payloadSerializer.serializePayload(groupedResults)
        ServerGroupedResults parsedGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        then:
        parsedGroupedResults.groupedTestSuites.size() == 3

        ServerGroupedTestSuites groupedTestSuites1 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup1" }
        groupedTestSuites1.groupName == "MyGroup1"
        groupedTestSuites1.groupLabel == "MyLabel1"
        groupedTestSuites1.directory == "path/to/my-group-1"
        groupedTestSuites1.testSuitesBlob == """<testsuites>Blob1</testsuites>"""

        ServerGroupedTestSuites groupedTestSuites2 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup2" }
        groupedTestSuites2.groupName == "MyGroup2"
        groupedTestSuites2.groupLabel == "MyLabel2"
        groupedTestSuites2.directory == "path/to/my-group-2"
        groupedTestSuites2.testSuitesBlob == """<testsuites>Blob2</testsuites>"""

        ServerGroupedTestSuites groupedTestSuites3 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup3" }
        groupedTestSuites3.groupName == "MyGroup3"
        groupedTestSuites3.groupLabel == "MyLabel3"
        groupedTestSuites3.directory == "path/to/my-group-3"
        groupedTestSuites3.testSuitesBlob == """<testsuites>Blob3</testsuites>"""
    }
}
