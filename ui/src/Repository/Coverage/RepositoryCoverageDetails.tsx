import * as React from "react";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryCoverageTimelineGraph from "./RepositoryCoverageTimelineGraph";
import { Typography } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import RepositoryCoverageBadge from "../../Badge/RepositoryCoverageBadge";

interface RepositoryCoverageDetailsProps {
  coverageTimeline: RepositoryCoverageTimeline;
  repoName: string;
  projectName?: string;
  hideIfEmpty?: boolean;
}

const useStyles = makeStyles(() => ({
  noCoverage: {
    marginTop: "30px",
  },
  coverageBadgeSection: {
    marginLeft: "20px",
  },
}));

const RepositoryCoverageDetails = ({
  coverageTimeline,
  repoName,
  projectName,
  hideIfEmpty,
}: RepositoryCoverageDetailsProps) => {
  const classes = useStyles({});

  if (coverageTimeline) {
    return (
      <div>
        <RepositoryCoverageTimelineGraph coverageTimeline={coverageTimeline} />
        <div className={classes.coverageBadgeSection}>
          <RepositoryCoverageBadge
            repoName={repoName}
            projectName={projectName}
          />
        </div>
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
