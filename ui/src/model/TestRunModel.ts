interface Attachment {
  fileName: string;
  objectName: string;
  fileSize?: number;
}

interface Attachments {
  attachments: Attachment[];
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
  hasAttachments: boolean;
}

interface TestRun extends TestRunSummary {
  id: string;
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
  Attachment,
  Attachments,
  TestRunSummary,
  TestRun,
  TestSuite,
  TestSuiteOutput,
  TestCase,
  TestFailure,
  TestResultsProcessingStatus,
  TestResultsProcessing
};
