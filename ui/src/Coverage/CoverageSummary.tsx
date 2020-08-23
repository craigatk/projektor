import * as React from "react";
import PageTitle from "../PageTitle";
import LoadingState from "../Loading/LoadingState";
import { CoverageStats } from "../model/TestRunModel";
import { fetchOverallCoverageStats } from "../service/TestRunService";
import OverallCoverageGraphs from "./OverallCoverageGraphs";

interface CoverageSummaryProps {
  publicId: string;
}

const CoverageSummary = ({ publicId }: CoverageSummaryProps) => {
  const [overallStats, setOverallStats] = React.useState<CoverageStats>(null);
  const [loadingState, setLoadingState] = React.useState(LoadingState.Loading);

  React.useEffect(() => {
    fetchOverallCoverageStats(publicId)
      .then((response) => {
        setOverallStats(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setOverallStats, setLoadingState]);

  if (overallStats) {
    return (
      <div>
        <PageTitle title="Coverage" testid="coverage-summary-title" />
        <OverallCoverageGraphs overallStats={overallStats} />
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageSummary;
