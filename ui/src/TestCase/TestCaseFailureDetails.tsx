import * as React from "react";
import classes from "./TestCaseFailureDetails.module.css";
import { TestFailure } from "../model/TestRunModel";
import { RouteComponentProps } from "@reach/router";

interface TestCaseFailureDetailsProps extends RouteComponentProps {
  failure: TestFailure;
}

const TestCaseFailureDetails = ({ failure }: TestCaseFailureDetailsProps) => {
  return (
    <div className={classes.failureContents}>
      <div>
        <pre data-testid="test-case-failure-text">{failure.failureText}</pre>
      </div>
    </div>
  );
};

export default TestCaseFailureDetails;
