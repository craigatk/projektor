import * as React from "react";
import PageTitle from "../PageTitle";
import LoadingState from "../Loading/LoadingState";
import { Coverage } from "../model/TestRunModel";
import { fetchCoverage } from "../service/TestRunService";
import OverallCoverageGraphs from "./OverallCoverageGraphs";

interface CoverageSummaryProps {
  publicId: string;
}

const CoverageSummary = ({ publicId }: CoverageSummaryProps) => {
  const [coverage, setCoverage] = React.useState<Coverage>(null);
  const [loadingState, setLoadingState] = React.useState(LoadingState.Loading);

  React.useEffect(() => {
    fetchCoverage(publicId)
      .then((response) => {
        setCoverage(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCoverage, setLoadingState]);

  if (coverage) {
    return (
      <div>
        <PageTitle title="Coverage" testid="coverage-summary-title" />
        <OverallCoverageGraphs
          overallStats={coverage.overallStats}
          previousTestRunId={coverage.previousTestRunId}
        />
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageSummary;
