import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { RouteComponentProps } from "@reach/router";
import { RepositoryTimeline } from "../../model/RepositoryModel";
import { fetchRepositoryTimeline } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import RepositoryTimelineDetails from "./RepositoryTimelineDetails";

interface RepositoryTimelinePageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const RepositoryTimelinePage = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryTimelinePageProps) => {
  const repoName = `${orgPart}/${repoPart}`;
  const [repositoryTimeline, setRepositoryTimeline] = React.useState<
    RepositoryTimeline
  >(null);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchRepositoryTimeline(repoName, projectName)
      .then((response) => {
        setRepositoryTimeline(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setRepositoryTimeline]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <RepositoryTimelineDetails
          timeline={repositoryTimeline}
          repoName={repoName}
        />
      }
    />
  );
};

export default RepositoryTimelinePage;
