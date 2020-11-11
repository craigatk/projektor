import * as React from "react";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { Typography } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import RepositoryFlakyTestsTable from "./RepositoryFlakyTestsTable";
import PageTitle from "../../PageTitle";

interface RepositoryFlakyTestsDetailsProps {
  flakyTests: RepositoryFlakyTests;
  repoName: string;
  hideIfEmpty: boolean;
}

const useStyles = makeStyles(() => ({
  noFlakyTests: {
    marginTop: "30px",
  },
}));

const RepositoryFlakyTestsDetails = ({
  flakyTests,
  repoName,
  hideIfEmpty,
}: RepositoryFlakyTestsDetailsProps) => {
  const classes = useStyles({});

  if (flakyTests) {
    return (
      <div>
        <PageTitle title="Flaky tests" testid="repository-flaky-tests-title" />
        <RepositoryFlakyTestsTable flakyTests={flakyTests} />
      </div>
    );
  } else if (!hideIfEmpty) {
    return (
      <div data-testid="repository-no-flaky-tests">
        <Typography align="center" className={classes.noFlakyTests}>
          No flaky tests found in repository {repoName}
        </Typography>
      </div>
    );
  } else {
    return <div></div>;
  }
};

export default RepositoryFlakyTestsDetails;
