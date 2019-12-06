interface TestFailure {
  failureMessage: String;
  failureType: String;
  failureText: String;
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
  packageName: String;
  className: String;
  testCount: number;
  passingCount: number;
  skippedCount: number;
  failureCount: number;
  startTs: Date;
  duration: number;
  testCases: TestCase[];
  hasSystemOut: boolean;
  hasSystemErr: boolean;
}

interface TestSuiteOutput {
  value: String;
}

interface TestRunSummary {
  id: String;
  totalTestCount: number;
  totalPassingCount: number;
  totalSkippedCount: number;
  totalFailureCount: number;
  passed: boolean;
  cumulativeDuration: number;
  averageDuration: number;
  slowestTestCaseDuration: number;
}

interface TestRun extends TestRunSummary {
  id: String;
  testSuites: TestSuite[];
}

enum TestResultsProcessingStatus {
  RECEIVED = "RECEIVED",
  PROCESSING = "PROCESSING",
  SUCCESS = "SUCCESS",
  ERROR = "ERROR"
}

interface TestResultsProcessing {
  id: string;
  status: TestResultsProcessingStatus;
  errorMessage?: string;
}

export {
  TestRunSummary,
  TestRun,
  TestSuite,
  TestSuiteOutput,
  TestCase,
  TestFailure,
  TestResultsProcessingStatus,
  TestResultsProcessing
};
