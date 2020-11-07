import * as React from "react";
import { PerformanceResult } from "../model/TestRunModel";
import PageTitle from "../PageTitle";
import PerformanceResultsTable from "./PerformanceResultsTable";

interface PerformanceDetailsProps {
  performanceResults: PerformanceResult[];
}

const PerformanceDetails = ({
  performanceResults,
}: PerformanceDetailsProps) => {
  if (performanceResults && performanceResults.length > 0) {
    return (
      <div data-testid="performance-results-details">
        <PageTitle
          title="Performance tests"
          testid="performance-results-title"
        />
        <PerformanceResultsTable performanceResults={performanceResults} />
      </div>
    );
  } else {
    return <div />;
  }
};

export default PerformanceDetails;
