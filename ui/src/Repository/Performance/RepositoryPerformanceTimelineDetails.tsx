import * as React from "react";
import { RepositoryPerformanceTimeline } from "../../model/RepositoryModel";
import RepositoryPerformanceTimelineGraph from "./RepositoryPerformanceTimelineGraph";
import PageTitle from "../../PageTitle";
import { Typography } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";

interface RepositoryPerformanceTimelineDetailsProps {
  performanceTimeline: RepositoryPerformanceTimeline;
  repoName: string;
  hideIfEmpty: boolean;
}

const useStyles = makeStyles(() => ({
  noData: {
    marginTop: "30px",
  },
  testTitle: {
    marginLeft: "15px",
  },
}));

const RepositoryPerformanceTimelineDetails = ({
  performanceTimeline,
  repoName,
  hideIfEmpty,
}: RepositoryPerformanceTimelineDetailsProps) => {
  const classes = useStyles({});

  if (performanceTimeline) {
    return (
      <div>
        <PageTitle
          title="Performance tests over time"
          testid="repository-performance-title"
        />
        {performanceTimeline.testTimelines.map((testTimeline, idx) => (
          <div>
            <Typography
              className={classes.testTitle}
              data-testid={`performance-timeline-title-${idx + 1}`}
            >
              {testTimeline.name}
            </Typography>
            <RepositoryPerformanceTimelineGraph
              performanceTestTimeline={testTimeline}
            />
          </div>
        ))}
      </div>
    );
  } else if (!hideIfEmpty) {
    return (
      <div data-testid="repository-no-performance">
        <Typography align="center" className={classes.noData}>
          No performance tests found for repository {repoName}
        </Typography>
      </div>
    );
  } else {
    return <div />;
  }
};

export default RepositoryPerformanceTimelineDetails;
