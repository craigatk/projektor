import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../Loading/LoadingState";
import { CoverageFiles } from "../model/TestRunModel";
import { fetchCoverageGroupFiles } from "../service/TestRunService";
import PageTitle from "../PageTitle";
import LoadingSection from "../Loading/LoadingSection";
import CoverageFilesTable from "./CoverageFilesTable";

interface CoverageGroupFilesPageProps extends RouteComponentProps {
  publicId: string;
  coverageGroupName: string;
}

const CoverageGroupFilesPage = ({
  publicId,
  coverageGroupName,
}: CoverageGroupFilesPageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [coverageFiles, setCoverageFiles] = React.useState<CoverageFiles>(null);

  React.useEffect(() => {
    fetchCoverageGroupFiles(publicId, coverageGroupName)
      .then((response) => {
        setCoverageFiles(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCoverageFiles, setLoadingState]);

  return (
    <div>
      <div>
        <PageTitle
          title={`Coverage for files in ${coverageGroupName}`}
          testid="coverage-group-files-title"
        />

        <LoadingSection
          loadingState={loadingState}
          successComponent={
            <CoverageFilesTable
              coverageFiles={coverageFiles}
              coverageGroupName={coverageGroupName}
            />
          }
        />
      </div>
    </div>
  );
};

export default CoverageGroupFilesPage;
