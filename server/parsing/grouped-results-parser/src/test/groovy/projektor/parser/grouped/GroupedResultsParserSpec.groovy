package projektor.parser.grouped

import com.fasterxml.jackson.databind.ObjectMapper
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.GroupedResults
import projektor.parser.grouped.model.GroupedTestSuites
import projektor.parser.grouped.model.PerformanceResult
import projektor.parser.grouped.model.ResultsMetadata
import spock.lang.Specification
import spock.lang.Subject

class GroupedResultsParserSpec extends Specification {
    @Subject
    GroupedResultsParser groupedResultsParser = new GroupedResultsParser()

    private ObjectMapper mapper = new ObjectMapper()

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

    def "should serialize grouped results"() {
        given:
        List<GroupedTestSuites> groupedTestSuites = (1..2).collect { idx ->
            new GroupedTestSuites(
                    groupName: "MyGroup${idx}",
                    groupLabel: "MyLabel${idx}",
                    directory: "path/to/my-group-${idx}",
                    testSuitesBlob: """<testsuites>Blob${idx}</testsuites>"""
            )
        }

        GroupedResults groupedResults = new GroupedResults(groupedTestSuites: groupedTestSuites)

        when:
        String serializedGroupedResults = groupedResultsParser.serializeGroupedResults(groupedResults)

        then:
        serializedGroupedResults != null

        GroupedResults parsedGroupedResults = groupedResultsParser.parseGroupedResults(serializedGroupedResults)

        parsedGroupedResults.groupedTestSuites.size() == 2

        parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup1" }
        parsedGroupedResults.groupedTestSuites.find { it.groupName == "MyGroup2" }
    }

    def "should parse grouped results with all metadata"() {
        given:
        List<GroupedTestSuites> groupedTestSuites = (1..2).collect { idx ->
            new GroupedTestSuites(
                    groupName: "MyGroup${idx}",
                    groupLabel: "MyLabel${idx}",
                    directory: "path/to/my-group-${idx}",
                    testSuitesBlob: """<testsuites>Blob${idx}</testsuites>"""
            )
        }

        GroupedResults groupedResults = new GroupedResults(
                groupedTestSuites: groupedTestSuites,
                metadata: new ResultsMetadata(
                        ci: true,
                        git: new GitMetadata(
                                repoName: "org/repo",
                                branchName: "main",
                                isMainBranch: true
                        )
                )
        )

        when:
        String groupedResultsXml = mapper.writeValueAsString(groupedResults)
        GroupedResults parsedGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        then:
        parsedGroupedResults.metadata.ci
        parsedGroupedResults.metadata.git.repoName == "org/repo"
        parsedGroupedResults.metadata.git.branchName == "main"
        parsedGroupedResults.metadata.git.isMainBranch

        and:
        parsedGroupedResults.groupedTestSuites.size() == 2
    }

    def "should deserialize results with only performance results"() {
        given:
        GroupedResults groupedResults = new GroupedResults()
        groupedResults.metadata = new ResultsMetadata(
                git: new GitMetadata(repoName: "my-repo", branchName: "main", isMainBranch: true)
        )
        groupedResults.performanceResults = [new PerformanceResult(name: "perf-1", resultsBlob: """
"metrics": {}
""")]
        String resultsJson = mapper.writeValueAsString(groupedResults)

        when:
        GroupedResults parsedResults = groupedResultsParser.parseGroupedResults(resultsJson)

        then:
        parsedResults.performanceResults.size() == 1
        groupedResults.performanceResults.find { it.name == "perf-1" }
    }
}
