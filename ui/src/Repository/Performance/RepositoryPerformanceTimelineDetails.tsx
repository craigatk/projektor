import * as React from "react";
import { RepositoryPerformanceTimeline } from "../../model/RepositoryModel";
import RepositoryPerformanceTimelineGraph from "./RepositoryPerformanceTimelineGraph";
import PageTitle from "../../PageTitle";
import { Typography } from "@material-ui/core";
import classes from "./RepositoryPerformanceTimelineDetails.module.css";

interface RepositoryPerformanceTimelineDetailsProps {
  performanceTimeline: RepositoryPerformanceTimeline;
  repoName: string;
  hideIfEmpty: boolean;
}

const RepositoryPerformanceTimelineDetails = ({
  performanceTimeline,
  repoName,
  hideIfEmpty,
}: RepositoryPerformanceTimelineDetailsProps) => {

  if (performanceTimeline) {
    return (
      <div>
        <PageTitle
          title="Performance tests over time"
          testid="repository-performance-title"
        />
        {performanceTimeline.testTimelines.map((testTimeline, idx) => (
          <div className={classes.testTitle}>
            <Typography
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
      <div data-testid="repository-no-performance" className={classes.noData}>
        <Typography align="center">
          No performance tests found for repository {repoName}
        </Typography>
      </div>
    );
  } else {
    return <div />;
  }
};

export default RepositoryPerformanceTimelineDetails;
