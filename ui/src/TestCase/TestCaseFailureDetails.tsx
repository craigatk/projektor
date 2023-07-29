import * as React from "react";
import { TestFailure } from "../model/TestRunModel";
import { RouteComponentProps } from "@reach/router";
import { makeStyles } from "@mui/material/styles";

interface TestCaseFailureDetailsProps extends RouteComponentProps {
  failure: TestFailure;
}

const useStyles = makeStyles(() => ({
  failureContents: {
    padding: "2px 10px",
    backgroundColor: "#EDEDED",
    borderRadius: "8px",
    overflowX: "auto",
    fontSize: "0.9em",
  },
}));

const TestCaseFailureDetails = ({ failure }: TestCaseFailureDetailsProps) => {
  const classes = useStyles({});

  return (
    <div className={classes.failureContents}>
      <div>
        <pre data-testid="test-case-failure-text">{failure.failureText}</pre>
      </div>
    </div>
  );
};

export default TestCaseFailureDetails;
