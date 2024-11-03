import * as React from "react";
import classes from "./TestCaseSummary.module.css";
import { TestCase } from "../model/TestRunModel";
import { RouteComponentProps } from "@reach/router";
import { Typography } from "@mui/material";
import PassedIcon from "../Icons/PassedIcon";
import SkippedIcon from "../Icons/SkippedIcon";
import FailedIcon from "../Icons/FailedIcon";

interface TestCaseSummaryProps extends RouteComponentProps {
  testCase: TestCase;
}
const TestCaseSummary = ({ testCase }: TestCaseSummaryProps) => {
  let resultStr: string;
  let resultIcon: React.ReactNode;

  if (testCase.passed) {
    resultStr = "passed";
    resultIcon = <PassedIcon className={classes.resultIcon} />;
  } else if (testCase.skipped) {
    resultStr = "skipped";
    resultIcon = <SkippedIcon className={classes.resultIcon} />;
  } else {
    resultStr = "failed";
    resultIcon = <FailedIcon className={classes.resultIcon} />;
  }

  return (
    <div>
      <Typography variant="body1">
        <span className={classes.label}>Result</span>
        <span data-testid="test-case-summary-result">
          {resultIcon} {resultStr}
        </span>
      </Typography>
      <Typography variant="body1">
        <span className={classes.label}>Name</span>
        <span data-testid="test-case-summary-name">{testCase.name}</span>
      </Typography>
      <Typography variant="body1">
        <span className={classes.label}>Class</span>
        <span data-testid="test-case-summary-class-name">
          {testCase.className}
        </span>
      </Typography>
      {testCase.packageName && (
        <Typography variant="body1">
          <span className={classes.label}>Package</span>
          <span data-testid="test-case-summary-package-name">
            {testCase.packageName}
          </span>
        </Typography>
      )}
      <Typography variant="body1">
        <span className={classes.label}>Duration</span>
        <span data-testid="test-case-summary-duration">
          {testCase.duration}s
        </span>
      </Typography>
    </div>
  );
};

export default TestCaseSummary;
