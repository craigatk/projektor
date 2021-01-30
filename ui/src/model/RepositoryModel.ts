import { CoverageStats, PerformanceResult, TestCase } from "./TestRunModel";

interface RepositoryTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  cumulativeDuration: number;
  totalTestCount: number;
  testAverageDuration: number;
}

interface RepositoryTimeline {
  timelineEntries: RepositoryTimelineEntry[];
}

interface RepositoryCoverageTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  coverageStats: CoverageStats;
}

interface RepositoryCoverageTimeline {
  timelineEntries: RepositoryCoverageTimelineEntry[];
}

interface RepositoryFlakyTest {
  testCase: TestCase;
  failureCount: number;
  failurePercentage: number;
  firstTestCase: TestCase;
  latestTestCase: TestCase;
}

interface RepositoryFlakyTests {
  tests: RepositoryFlakyTest[];
  maxRuns: number;
  failureCountThreshold: number;
}

interface RepositoryPerformanceTestTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  performanceResult: PerformanceResult;
}

interface RepositoryPerformanceTestTimeline {
  name: string;
  entries: RepositoryPerformanceTestTimelineEntry[];
}

interface RepositoryPerformanceTimeline {
  testTimelines: RepositoryPerformanceTestTimeline[];
}

export {
  RepositoryTimelineEntry,
  RepositoryTimeline,
  RepositoryCoverageTimeline,
  RepositoryCoverageTimelineEntry,
  RepositoryFlakyTest,
  RepositoryFlakyTests,
  RepositoryPerformanceTestTimelineEntry,
  RepositoryPerformanceTestTimeline,
  RepositoryPerformanceTimeline,
};
