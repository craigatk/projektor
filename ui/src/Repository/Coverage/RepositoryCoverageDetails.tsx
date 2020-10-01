import * as React from "react";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryCoverageTimelineGraph from "./RepositoryCoverageTimelineGraph";
import { Typography } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";

interface RepositoryCoverageDetailsProps {
  coverageTimeline: RepositoryCoverageTimeline;
  repoName: string;
  hideIfEmpty?: boolean;
}

const useStyles = makeStyles(() => ({
  noCoverage: {
    marginTop: "30px",
  },
}));

const RepositoryCoverageDetails = ({
  coverageTimeline,
  repoName,
  hideIfEmpty,
}: RepositoryCoverageDetailsProps) => {
  const classes = useStyles({});

  if (coverageTimeline) {
    return (
      <div>
        <PageTitle
          title="Coverage over time"
          testid="repository-coverage-title"
        />
        <RepositoryCoverageTimelineGraph coverageTimeline={coverageTimeline} />
      </div>
    );
  } else {
    if (hideIfEmpty) {
      return null;
    } else {
      return (
        <Typography
          align="center"
          data-testid="repo-no-coverage"
          className={classes.noCoverage}
        >
          No coverage information available for repository {repoName}
        </Typography>
      );
    }
  }
};

export default RepositoryCoverageDetails;
