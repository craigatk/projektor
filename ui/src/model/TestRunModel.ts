interface Attachment {
  fileName: string;
  objectName: string;
  fileSize?: number;
}

interface Attachments {
  attachments: Attachment[];
}

interface CoverageStat {
  covered: number;
  missed: number;
  total: number;
  coveredPercentage: number;
  coveredPercentageDelta?: number;
}

interface CoverageStats {
  branchStat: CoverageStat;
  lineStat: CoverageStat;
  statementStat: CoverageStat;
}

interface CoverageGroup {
  name: string;
  stats: CoverageStats;
}

interface Coverage {
  groups: CoverageGroup[];
  overallStats: CoverageStats;
  previousTestRunId?: string;
}

interface CoverageExists {
  exists: boolean;
}

interface Messages {
  messages: string[];
}

interface TestFailure {
  failureMessage: string;
  failureType: string;
  failureText: string;
}

interface TestCase {
  idx: number;
  testSuiteIdx: number;
  name: string;
  packageName: string;
  className: string;
  duration: number;
  passed: boolean;
  skipped: boolean;
  failure: TestFailure;
  hasSystemOut: boolean;
  hasSystemErr: boolean;
}

interface TestSuite {
  idx: number;
  packageName: string;
  className: string;
  testCount: number;
  passingCount: number;
  skippedCount: number;
  failureCount: number;
  startTs: Date;
  duration: number;
  testCases: TestCase[];
  hasSystemOut: boolean;
  hasSystemErr: boolean;
  groupName?: string;
  groupLabel?: string;
}

interface TestSuiteOutput {
  value: string;
}

interface TestRunSummary {
  id: string;
  totalTestCount: number;
  totalPassingCount: number;
  totalSkippedCount: number;
  totalFailureCount: number;
  passed: boolean;
  cumulativeDuration: number;
  averageDuration: number;
  slowestTestCaseDuration: number;
  createdTimestamp: Date;
}

interface TestRun extends TestRunSummary {
  id: string;
  testSuites: TestSuite[];
}

enum TestResultsProcessingStatus {
  RECEIVED = "RECEIVED",
  PROCESSING = "PROCESSING",
  SUCCESS = "SUCCESS",
  DELETED = "DELETED",
  ERROR = "ERROR",
}

interface TestResultsProcessing {
  id: string;
  status: TestResultsProcessingStatus;
  errorMessage?: string;
}

interface TestRunSystemAttributes {
  pinned: boolean;
}

export {
  Attachment,
  Attachments,
  Coverage,
  CoverageExists,
  CoverageGroup,
  CoverageStat,
  CoverageStats,
  Messages,
  TestRunSummary,
  TestRun,
  TestSuite,
  TestSuiteOutput,
  TestCase,
  TestFailure,
  TestResultsProcessingStatus,
  TestResultsProcessing,
  TestRunSystemAttributes,
};
