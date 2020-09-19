import * as React from "react";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryCoverageTimelineGraph from "./RepositoryCoverageTimelineGraph";
import { Typography } from "@material-ui/core";

interface RepositoryCoverageDetailsProps {
  coverageTimeline: RepositoryCoverageTimeline;
  repoName: string;
}

const RepositoryCoverageDetails = ({
  coverageTimeline,
  repoName,
}: RepositoryCoverageDetailsProps) => {
  return (
    <div>
      <PageTitle
        title="Coverage over time"
        testid="repository-coverage-title"
      />
      {coverageTimeline ? (
        <RepositoryCoverageTimelineGraph coverageTimeline={coverageTimeline} />
      ) : (
        <Typography align="center" data-testid="repo-no-coverage">
          No coverage information available for repository {repoName}
        </Typography>
      )}
    </div>
  );
};

export default RepositoryCoverageDetails;
