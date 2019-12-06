import { TestCase } from "../model/TestRunModel";

const sortTestSuiteTestCases = (testCases: TestCase[]): TestCase[] => {
  testCases.sort((a, b) => {
    if (a.failure || b.failure) {
      return a.failure ? -1 : 1;
    } else if (a.skipped || b.skipped) {
      return a.skipped ? 1 : -1;
    } else {
      return 0;
    }
  });

  return testCases;
};

export { sortTestSuiteTestCases };
