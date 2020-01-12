package projektor.plugin.results.grouped

import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.grouped.model.GroupedResults as ServerGroupedResults
import projektor.parser.grouped.model.GroupedTestSuites as ServerGroupedTestSuites
import spock.lang.Specification
import spock.lang.Subject

class GroupedResultsSerializerSpec extends Specification {
    @Subject
    GroupedResultsSerializer groupedResultsSerializer = new GroupedResultsSerializer()

    private GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    def "should serialize grouped results with one test suite as a string"() {
        given:
        GroupedTestSuites groupedTestSuites = new GroupedTestSuites()
        groupedTestSuites.groupLabel = "MyProject"
        groupedTestSuites.groupName = "MyGroup"
        groupedTestSuites.directory = "path/to/my-project"
        groupedTestSuites.testSuitesBlob = """<testsuites>Yes</testsuites>"""

        GroupedResults groupedResults = new GroupedResults()
        groupedResults.groupedTestSuites = [groupedTestSuites]

        when:
        String groupedResultsXml = groupedResultsSerializer.serializeGroupedResults(groupedResults)

        then:
        groupedResultsXml == """<GroupedResults><groupedTestSuites><groupName>MyGroup</groupName><groupLabel>MyProject</groupLabel><directory>path/to/my-project</directory><testSuitesBlob><![CDATA[<testsuites>Yes</testsuites>]]></testSuitesBlob></groupedTestSuites></GroupedResults>"""
    }

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
        String groupedResultsXml = groupedResultsSerializer.serializeGroupedResults(groupedResults)
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
