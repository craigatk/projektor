import * as React from "react";
import { RepositoryPerformanceTimeline } from "../../model/RepositoryModel";
import RepositoryPerformanceTimelineGraph from "./RepositoryPerformanceTimelineGraph";
import PageTitle from "../../PageTitle";

interface RepositoryPerformanceTimelineDetailsProps {
  performanceTimeline: RepositoryPerformanceTimeline;
}

const RepositoryPerformanceTimelineDetails = ({
  performanceTimeline,
}: RepositoryPerformanceTimelineDetailsProps) => {
  if (performanceTimeline) {
    return (
      <div>
        <PageTitle
          title="Performance over time"
          testid="repository-performance-title"
        />
        {performanceTimeline.testTimelines.map((testTimeline) => (
          <RepositoryPerformanceTimelineGraph
            performanceTestTimeline={testTimeline}
          />
        ))}
      </div>
    );
  } else {
    return <div></div>;
  }
};

export default RepositoryPerformanceTimelineDetails;
