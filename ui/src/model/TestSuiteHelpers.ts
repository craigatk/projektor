import { TestSuite } from "./TestRunModel";

const anyTestSuiteHasGroupName = (testSuites: TestSuite[]): boolean => {
  return testSuites.filter(testSuiteHasGroupName).length > 0;
};

const testSuiteHasGroupName = (testSuite: TestSuite): boolean => {
  return !!testSuite.groupName;
};

const fullTestSuiteName = (testSuite: TestSuite): string => {
  let fullName = testSuite.className;

  if (testSuite.packageName) {
    fullName = testSuite.packageName + "." + fullName;
  }

  return fullName;
};

export { anyTestSuiteHasGroupName, testSuiteHasGroupName, fullTestSuiteName };
