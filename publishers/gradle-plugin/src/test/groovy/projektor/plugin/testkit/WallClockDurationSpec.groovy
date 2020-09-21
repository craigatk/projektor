package projektor.plugin.testkit

import projektor.plugin.SpecWriter
import projektor.plugin.results.grouped.GroupedResults

class WallClockDurationSpec extends SingleProjectSpec {
    def "should publish wall clock duration"() {
        given:
        buildFile << """
            projektor {
                serverUrl = '${serverUrl}'
                publishToken = 'publish12345'
            }
        """.stripIndent()

        SpecWriter.createTestDirectoryWithFailingTests(projectRootDir, ["FirstSpec", "SecondSpec", "ThirdSpec"])

        String resultsId = "DGA423"
        resultsStubber.stubResultsPostSuccess(resultsId)

        when:
        runFailedBuild('test')

        then:
        List<GroupedResults> groupedResultsBodies = resultsStubber.findResultsRequestBodies()
        groupedResultsBodies.size() == 1

        BigDecimal wallClockDuration = groupedResultsBodies[0].wallClockDuration
        println "Wall clock duration: ${wallClockDuration}"

        wallClockDuration != null
        wallClockDuration > BigDecimal.ZERO
    }
}
