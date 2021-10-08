package projektor.parser.grouped.model;

import java.math.BigDecimal;
import java.util.List;

public class GroupedResults {
    public List<GroupedTestSuites> groupedTestSuites;
    public List<PerformanceResult> performanceResults;
    public ResultsMetadata metadata;
    public BigDecimal wallClockDuration;
    public List<CoverageFile> coverageFiles;
    public List<CodeQualityReport> codeQualityFiles;
}
