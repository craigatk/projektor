import * as React from "react";
import { TestSuite } from "../model/TestRunModel";
import { RouteComponentProps } from "@reach/router";
import TestCountList from "../TestCount/TestCountList";
import TestCaseList from "../TestCase/list/TestCaseList";
import { sortTestSuiteTestCases } from "./sort";

interface TestSuiteTestCaseListProps extends RouteComponentProps {
  publicId: string;
  testSuite: TestSuite;
}

const TestSuiteTestCaseList = ({
  publicId,
  testSuite
}: TestSuiteTestCaseListProps) => {
  const { testCases } = testSuite;
  const sortedTestCases = sortTestSuiteTestCases(testCases);

  return (
    <div>
      <TestCountList
        passedCount={testSuite.passingCount}
        failedCount={testSuite.failureCount}
        skippedCount={testSuite.skippedCount}
        totalCount={testSuite.testCount}
        horizontal={true}
      />
      <TestCaseList
        publicId={publicId}
        testCases={sortedTestCases}
        showFullTestCaseName={false}
      />
    </div>
  );
};

export default TestSuiteTestCaseList;
