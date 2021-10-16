import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../Loading/LoadingState";
import { CoverageFiles, TestRunGitMetadata } from "../model/TestRunModel";
import {
  fetchCoverageGroupFiles,
  fetchTestRunGitMetadata,
} from "../service/TestRunService";
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
  const [gitMetadata, setGitMetadata] =
    React.useState<TestRunGitMetadata>(null);

  React.useEffect(() => {
    fetchCoverageGroupFiles(publicId, coverageGroupName)
      .then((response) => {
        setCoverageFiles(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCoverageFiles, setLoadingState]);

  React.useEffect(() => {
    fetchTestRunGitMetadata(publicId)
      .then((response) => {
        setGitMetadata(response.data);
      })
      .catch(() => {});
  }, [setGitMetadata]);

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
              gitMetadata={gitMetadata}
            />
          }
        />
      </div>
    </div>
  );
};

export default CoverageGroupFilesPage;
