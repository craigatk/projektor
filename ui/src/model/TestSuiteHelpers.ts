import { TestSuite } from "./TestRunModel";

const anyTestSuiteHasGroupName = (testSuites: TestSuite[]): boolean => {
  return testSuites.filter(testSuiteHasGroupName).length > 0;
};

const testSuiteHasGroupName = (testSuite: TestSuite): boolean => {
  return !!testSuite.groupName;
};

const fullTestSuiteName = (testSuite: TestSuite): string => {
  if (testSuite.packageName) {
    if (testSuite.packageName.includes(testSuite.className)) {
      return testSuite.packageName;
    } else {
      return testSuite.packageName + "." + testSuite.className;
    }
  } else {
    return testSuite.className;
  }
};

export { anyTestSuiteHasGroupName, testSuiteHasGroupName, fullTestSuiteName };
