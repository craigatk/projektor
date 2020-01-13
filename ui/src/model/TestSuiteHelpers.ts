import { TestSuite } from "./TestRunModel";

const anyTestSuiteHasGroupName = (testSuites: TestSuite[]) => {
  return testSuites.filter(testSuiteHasGroupName).length > 0;
};

const testSuiteHasGroupName = (testSuite: TestSuite) => {
  return testSuite.groupName;
};

export { anyTestSuiteHasGroupName, testSuiteHasGroupName };
