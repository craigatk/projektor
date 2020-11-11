import * as React from "react";
import LoadingState from "../../Loading/LoadingState";
import { RepositoryPerformanceTimeline } from "../../model/RepositoryModel";
import { fetchRepositoryPerformanceTimeline } from "../../service/RepositoryService";
import LoadingSection from "../../Loading/LoadingSection";
import RepositoryPerformanceTimelineDetails from "./RepositoryPerformanceTimelineDetails";
import { RouteComponentProps } from "@reach/router";

interface RepositoryPerformanceTimelinePageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
  hideIfEmpty: boolean;
}

const RepositoryPerformanceTimelinePage = ({
  orgPart,
  repoPart,
  projectName,
  hideIfEmpty,
}: RepositoryPerformanceTimelinePageProps) => {
  const repoName = `${orgPart}/${repoPart}`;

  const [
    repositoryPerformanceTimeline,
    setRepositoryPerformanceTimeline,
  ] = React.useState<RepositoryPerformanceTimeline>(null);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchRepositoryPerformanceTimeline(repoName, projectName)
      .then((response) => {
        setRepositoryPerformanceTimeline(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setRepositoryPerformanceTimeline]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <RepositoryPerformanceTimelineDetails
          performanceTimeline={repositoryPerformanceTimeline}
          repoName={repoName}
          hideIfEmpty={hideIfEmpty}
        />
      }
    />
  );
};

export default RepositoryPerformanceTimelinePage;
