import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { fetchRepositoryCoverageTimeline } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import { RouteComponentProps } from "@reach/router";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import RepositoryCoverageDetails from "./RepositoryCoverageDetails";

interface RepositoryCoveragePageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const RepositoryCoveragePage = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryCoveragePageProps) => {
  const repoName = `${orgPart}/${repoPart}`;
  const [
    repositoryCoverageTimeline,
    setRepositoryCoverageTimeline,
  ] = React.useState<RepositoryCoverageTimeline>(null);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchRepositoryCoverageTimeline(repoName, projectName)
      .then((response) => {
        setRepositoryCoverageTimeline(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setRepositoryCoverageTimeline]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <RepositoryCoverageDetails
          coverageTimeline={repositoryCoverageTimeline}
        />
      }
    />
  );
};

export default RepositoryCoveragePage;
