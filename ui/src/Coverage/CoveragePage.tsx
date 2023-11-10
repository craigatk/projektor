import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../Loading/LoadingState";
import { Coverage } from "../model/TestRunModel";
import { fetchCoverage } from "../service/TestRunService";
import PageTitle from "../PageTitle";
import LoadingSection from "../Loading/LoadingSection";
import CoverageDetails from "./CoverageDetails";

interface CoveragePageProps extends RouteComponentProps {
  publicId: string;
}

const CoveragePage = ({ publicId }: CoveragePageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [coverage, setCoverage] = React.useState<Coverage>(null);

  React.useEffect(() => {
    fetchCoverage(publicId)
      .then((response) => {
        setCoverage(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCoverage, setLoadingState]);

  return (
    <div>
      <PageTitle title="Coverage" testid="coverage-title" />

      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <CoverageDetails publicId={publicId} coverage={coverage} />
        }
      />
    </div>
  );
};

export default CoveragePage;
