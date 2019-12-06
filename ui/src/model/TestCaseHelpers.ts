import { TestCase } from "./TestRunModel";

const fullTestCaseName = (testCase: TestCase) => {
  let fullName = testCase.name;

  if (testCase.className) {
    fullName = testCase.className + "." + fullName;
  }

  if (testCase.packageName) {
    fullName = testCase.packageName + "." + fullName;
  }

  return fullName;
};

export { fullTestCaseName };
