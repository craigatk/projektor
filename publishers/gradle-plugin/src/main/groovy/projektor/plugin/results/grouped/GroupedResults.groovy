package projektor.plugin.results.grouped

import projektor.plugin.coverage.model.CoverageFilePayload

class GroupedResults {
    List<GroupedTestSuites> groupedTestSuites = []
    ResultsMetadata metadata
    BigDecimal wallClockDuration
    List<CoverageFilePayload> coverageFiles = []
}
