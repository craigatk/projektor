import { CoverageStats, TestCase } from "./TestRunModel";

interface RepositoryTimelineEntry {
  publicId: string;
  createdTimestamp: Date;
  cumulativeDuration: Date;
  totalTestCount: number;
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
  latestPublicId: string;
  latestCreatedTimestamp: Date;
}

interface RepositoryFlakyTests {
  tests: RepositoryFlakyTest[];
  maxRuns: number;
  failureCountThreshold: number;
}

export {
  RepositoryTimelineEntry,
  RepositoryTimeline,
  RepositoryCoverageTimeline,
  RepositoryCoverageTimelineEntry,
  RepositoryFlakyTest,
  RepositoryFlakyTests,
};
