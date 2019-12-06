import * as React from "react";
import TestSuiteList from "./TestSuiteList";
import { TestSuite } from "../model/TestRunModel";
import PageTitle from "../PageTitle";
import { Paper } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";

interface TestSuitePackageDetailsProps {
  publicId: string;
  packageName: string;
  testSuiteSummaries: TestSuite[];
}

const useStyles = makeStyles(theme => ({
  paper: {
    padding: theme.spacing(1, 2)
  }
}));

const TestSuitePackageDetails = ({
  publicId,
  packageName,
  testSuiteSummaries
}: TestSuitePackageDetailsProps) => {
  const classes = useStyles({});

  return (
    <div data-testid="test-suite-package-details">
      <PageTitle
        title={`Tests in package ${packageName}`}
        testid={`test-suite-package-name-header`}
      />
      <Paper elevation={1} className={classes.paper}>
        <TestSuiteList publicId={publicId} testSuites={testSuiteSummaries} />
      </Paper>
    </div>
  );
};

export default TestSuitePackageDetails;
