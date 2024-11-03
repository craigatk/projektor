import * as React from "react";
import classes from "./RepositoryCoverageDetails.module.css";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import RepositoryCoverageTimelineGraph from "./RepositoryCoverageTimelineGraph";
import { Typography } from "@mui/material";
import RepositoryCoverageBadge from "../../Badge/coverage/RepositoryCoverageBadge";

interface RepositoryCoverageDetailsProps {
  coverageTimeline: RepositoryCoverageTimeline;
  repoName: string;
  projectName?: string;
  hideIfEmpty?: boolean;
}

const RepositoryCoverageDetails = ({
  coverageTimeline,
  repoName,
  projectName,
  hideIfEmpty,
}: RepositoryCoverageDetailsProps) => {
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
        <div className={classes.noCoverage}>
          <Typography align="center" data-testid="repo-results-no-coverage">
            No coverage information available for repository {repoName}
          </Typography>
        </div>
      );
    }
  }
};

export default RepositoryCoverageDetails;
