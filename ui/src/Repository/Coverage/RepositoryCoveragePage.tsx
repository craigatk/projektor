import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { fetchRepositoryCoverageExists } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import { RouteComponentProps } from "@reach/router";
import RepositoryCoverageSearch from "./RepositoryCoverageSearch";

interface RepositoryCoveragePageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
  hideIfEmpty?: boolean;
}

const RepositoryCoveragePage = ({
  orgPart,
  repoPart,
  projectName,
  hideIfEmpty,
}: RepositoryCoveragePageProps) => {
  const repoName = `${orgPart}/${repoPart}`;
  const [coverageExists, setCoverageExists] = React.useState<boolean>(false);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );

  const doFetch = () => {
    fetchRepositoryCoverageExists(repoName, projectName)
      .then((response) => {
        setCoverageExists(response.data.exists);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(doFetch, [setCoverageExists, setLoadingState]);

  return (
    <div>
      <LoadingSection
        loadingState={loadingState}
        successComponent={
          coverageExists ? (
            <RepositoryCoverageSearch
              repoName={repoName}
              projectName={projectName}
              hideIfEmpty={hideIfEmpty}
            />
          ) : (
            <span data-testid="repo-page-no-coverage" />
          )
        }
      />
    </div>
  );
};

export default RepositoryCoveragePage;
