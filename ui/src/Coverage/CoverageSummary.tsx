import * as React from "react";
import PageTitle from "../PageTitle";
import LoadingState from "../Loading/LoadingState";
import { Coverage } from "../model/TestRunModel";
import { fetchCoverage } from "../service/TestRunService";
import OverallCoverageGraphs from "./OverallCoverageGraphs";
import CleanLink from "../Link/CleanLink";

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
        <CleanLink to={`/tests/${publicId}/coverage`}>
          <PageTitle title="Coverage" testid="coverage-summary-title" />
        </CleanLink>
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
