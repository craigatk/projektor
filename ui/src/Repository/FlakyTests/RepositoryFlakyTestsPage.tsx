import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { RouteComponentProps } from "@reach/router";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { fetchRepositoryFlakyTests } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import RepositoryFlakyTestsDetails from "./RepositoryFlakyTestsDetails";

interface RepositoryFlakyTestsPageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const RepositoryFlakyTestsPage = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryFlakyTestsPageProps) => {
  const repoName = `${orgPart}/${repoPart}`;

  const [flakyTests, setFlakyTests] = React.useState<RepositoryFlakyTests>(
    null
  );
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchRepositoryFlakyTests(repoName, projectName)
      .then((response) => {
        setFlakyTests(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setFlakyTests]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <RepositoryFlakyTestsDetails
          flakyTests={flakyTests}
          repoName={repoName}
        />
      }
    />
  );
};

export default RepositoryFlakyTestsPage;
