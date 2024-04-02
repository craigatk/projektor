import * as React from "react";
import { RepositoryTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryTimelineGraph from "./RepositoryTimelineGraph";
import { Typography } from "@material-ui/core";
import RepositoryTestsBadge from "../../Badge/tests/RepositoryTestsBadge";
import { makeStyles } from "@material-ui/styles";

interface RepositoryTimelineDetailsProps {
  timeline: RepositoryTimeline;
  repoName: string;
  projectName?: string;
  hideIfEmpty: boolean;
}

const useStyles = makeStyles(() => ({
  badgeSection: {
    marginLeft: "20px",
  },
}));

const RepositoryTimelineDetails = ({
  timeline,
  repoName,
  projectName,
  hideIfEmpty,
}: RepositoryTimelineDetailsProps) => {
  const classes = useStyles({});

  return (
    <div>
      {timeline ? (
        <PageTitle
          title="Test execution time"
          testid="repository-timeline-title"
        />
      ) : null}
      {timeline ? (
        <div>
          <RepositoryTimelineGraph timeline={timeline} />
          <div className={classes.badgeSection}>
            <RepositoryTestsBadge
              repoName={repoName}
              projectName={projectName}
            />
          </div>
        </div>
      ) : hideIfEmpty ? null : (
        <Typography align="center" data-testid="repo-no-timeline">
          No test execution information available for repository {repoName}
        </Typography>
      )}
    </div>
  );
};

export default RepositoryTimelineDetails;
