import * as React from "react";
import { TestFailure } from "../model/TestRunModel";
import { RouteComponentProps } from "@reach/router";

interface TestCaseFailureDetailsProps extends RouteComponentProps {
  failure: TestFailure;
}

const TestCaseFailureDetails = ({ failure }: TestCaseFailureDetailsProps) => {
  return (
    <div>
      <div>
        <pre>{failure.failureType}</pre>
      </div>
      <div>
        <pre data-testid="test-case-failure-text">{failure.failureText}</pre>
      </div>
    </div>
  );
};

export default TestCaseFailureDetails;
