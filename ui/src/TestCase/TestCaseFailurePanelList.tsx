import * as React from "react";
import { TestCase } from "../model/TestRunModel";
import TestCaseFailurePanel from "./TestCaseFailurePanel";

interface TestCaseFailurePanelListProps {
  testCases: TestCase[];
  publicId: String;
}

const TestCaseFailurePanelList = ({
  testCases,
  publicId
}: TestCaseFailurePanelListProps) => {
  return (
    <div>
      {testCases.map(testCase => (
        <TestCaseFailurePanel
          testCase={testCase}
          publicId={publicId}
          key={`test-case-${testCase.idx}`}
        />
      ))}
    </div>
  );
};

export default TestCaseFailurePanelList;
