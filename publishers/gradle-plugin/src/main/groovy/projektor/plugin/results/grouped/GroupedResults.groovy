package projektor.plugin.results.grouped

import projektor.plugin.coverage.model.CoverageFilePayload
import projektor.plugin.quality.CodeQualityFilePayload

class GroupedResults {
    List<GroupedTestSuites> groupedTestSuites = []
    ResultsMetadata metadata
    BigDecimal wallClockDuration

    List<CoverageFilePayload> coverageFiles = []

    List<CodeQualityFilePayload> codeQualityFiles = []
}
