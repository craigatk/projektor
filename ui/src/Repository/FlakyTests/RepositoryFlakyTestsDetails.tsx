import * as React from "react";
import classes from "./RepositoryFlakyTestsDetails.module.css";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { Typography } from "@material-ui/core";
import RepositoryFlakyTestsTable from "./RepositoryFlakyTestsTable";

interface RepositoryFlakyTestsDetailsProps {
  flakyTests: RepositoryFlakyTests;
  repoName: string;
  hideIfEmpty: boolean;
}

const RepositoryFlakyTestsDetails = ({
  flakyTests,
  repoName,
  hideIfEmpty,
}: RepositoryFlakyTestsDetailsProps) => {
  if (flakyTests) {
    return (
      <div>
        <RepositoryFlakyTestsTable flakyTests={flakyTests} />
      </div>
    );
  } else if (!hideIfEmpty) {
    return (
      <div
        data-testid="repository-no-flaky-tests"
        className={classes.noFlakyTests}
      >
        <Typography align="center">
          No flaky tests found in repository {repoName}
        </Typography>
      </div>
    );
  } else {
    return <div></div>;
  }
};

export default RepositoryFlakyTestsDetails;
