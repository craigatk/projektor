package projektor.parser.grouped

import projektor.parser.grouped.model.GroupedResults
import projektor.parser.grouped.model.GroupedTestSuites
import spock.lang.Specification
import spock.lang.Subject

class GroupedResultsParserSerializeSpec extends Specification {
    @Subject
    GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    def "should serialize grouped results with one test suite as a string"() {
        given:
        GroupedTestSuites groupedTestSuites = new GroupedTestSuites()
        groupedTestSuites.projectName = "MyProject"
        groupedTestSuites.groupName = "MyGroup"
        groupedTestSuites.directory = "my-project"
        groupedTestSuites.path = "path/to/my-project"
        groupedTestSuites.testSuitesBlob = """<testsuites>Yes</testsuites>"""

        GroupedResults groupedResults = new GroupedResults()
        groupedResults.groupedTestSuites = [groupedTestSuites]

        when:
        String groupedResultsXml = groupedResultsParser.serializeGroupedResults(groupedResults)

        then:
        groupedResultsXml == """<GroupedResults><groupedTestSuites><projectName>MyProject</projectName><groupName>MyGroup</groupName><directory>my-project</directory><path>path/to/my-project</path><testSuitesBlob><![CDATA[<testsuites>Yes</testsuites>]]></testSuitesBlob></groupedTestSuites></GroupedResults>"""
    }

    def "should deserialize grouped results that were serialized"() {
        given:
        List<GroupedTestSuites> groupedTestSuites = (1..3).collect { idx ->
            new GroupedTestSuites(
                    projectName: "MyProject${idx}",
                    groupName: "MyGroup${idx}",
                    directory: "my-group-${idx}",
                    path: "path/to/my-group-${idx}",
                    testSuitesBlob: """<testsuites>Blob${idx}</testsuites>"""
            )
        }

        GroupedResults groupedResults = new GroupedResults(groupedTestSuites: groupedTestSuites)

        when:
        String groupedResultsXml = groupedResultsParser.serializeGroupedResults(groupedResults)
        GroupedResults parsedGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        then:
        parsedGroupedResults.groupedTestSuites.size() == 3

        GroupedTestSuites groupedTestSuites1 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup1" }
        groupedTestSuites1.projectName == "MyProject1"
        groupedTestSuites1.groupName == "MyGroup1"
        groupedTestSuites1.directory == "my-group-1"
        groupedTestSuites1.path == "path/to/my-group-1"
        groupedTestSuites1.testSuitesBlob == """<testsuites>Blob1</testsuites>"""

        GroupedTestSuites groupedTestSuites2 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup2" }
        groupedTestSuites2.projectName == "MyProject2"
        groupedTestSuites2.groupName == "MyGroup2"
        groupedTestSuites2.directory == "my-group-2"
        groupedTestSuites2.path == "path/to/my-group-2"
        groupedTestSuites2.testSuitesBlob == """<testsuites>Blob2</testsuites>"""

        GroupedTestSuites groupedTestSuites3 = parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup3" }
        groupedTestSuites3.projectName == "MyProject3"
        groupedTestSuites3.groupName == "MyGroup3"
        groupedTestSuites3.directory == "my-group-3"
        groupedTestSuites3.path == "path/to/my-group-3"
        groupedTestSuites3.testSuitesBlob == """<testsuites>Blob3</testsuites>"""
    }
}
