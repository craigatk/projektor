import * as React from "react";
import classes from "./TestSuitePackageDetails.module.css";
import TestSuiteList from "./TestSuiteList";
import { TestSuite } from "../model/TestRunModel";
import PageTitle from "../PageTitle";
import { Paper } from "@mui/material";

interface TestSuitePackageDetailsProps {
  publicId: string;
  packageName: string;
  testSuiteSummaries: TestSuite[];
}

const TestSuitePackageDetails = ({
  publicId,
  packageName,
  testSuiteSummaries,
}: TestSuitePackageDetailsProps) => {
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
