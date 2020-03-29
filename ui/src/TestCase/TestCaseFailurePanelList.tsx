import * as React from "react";
import { TestCase } from "../model/TestRunModel";
import TestCaseFailurePanel from "./TestCaseFailurePanel";

const showFullFailureMaxFailureCount = 5;

interface TestCaseFailurePanelListProps {
  failedTestCases: TestCase[];
  publicId: string;
}

const TestCaseFailurePanelList = ({
  failedTestCases,
  publicId,
}: TestCaseFailurePanelListProps) => {
  return (
    <div>
      {failedTestCases.map((testCase) => (
        <TestCaseFailurePanel
          testCase={testCase}
          publicId={publicId}
          key={`test-case-${testCase.testSuiteIdx}-${testCase.idx}`}
          showFullFailure={
            failedTestCases.length <= showFullFailureMaxFailureCount
          }
        />
      ))}
    </div>
  );
};

export default TestCaseFailurePanelList;
