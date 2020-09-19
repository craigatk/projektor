import * as React from "react";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import PageTitle from "../../PageTitle";
import RepositoryCoverageTimelineGraph from "./RepositoryCoverageTimelineGraph";

interface RepositoryCoverageDetailsProps {
  coverageTimeline: RepositoryCoverageTimeline;
}

const RepositoryCoverageDetails = ({
  coverageTimeline,
}: RepositoryCoverageDetailsProps) => {
  return (
    <div>
      <PageTitle
        title="Coverage over time"
        testid="repository-coverage-title"
      />
      <RepositoryCoverageTimelineGraph coverageTimeline={coverageTimeline} />
    </div>
  );
};

export default RepositoryCoverageDetails;
