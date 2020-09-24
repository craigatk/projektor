import * as React from "react";
import { RepositoryTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryTimelineGraph from "./RepositoryTimelineGraph";
import { Typography } from "@material-ui/core";

interface RepositoryTimelineDetailsProps {
  timeline: RepositoryTimeline;
  repoName: string;
}

const RepositoryTimelineDetails = ({
  timeline,
  repoName,
}: RepositoryTimelineDetailsProps) => {
  return (
    <div>
      <PageTitle
        title="Test execution time"
        testid="repository-timeline-title"
      />
      {timeline ? (
        <RepositoryTimelineGraph timeline={timeline} />
      ) : (
        <Typography align="center" data-testid="repo-no-timeline">
          No test execution information available for repository {repoName}
        </Typography>
      )}
    </div>
  );
};

export default RepositoryTimelineDetails;
