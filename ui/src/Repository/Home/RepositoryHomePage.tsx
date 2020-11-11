import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import RepositoryTimelinePage from "../Timeline/RepositoryTimelinePage";
import RepositoryCoveragePage from "../Coverage/RepositoryCoveragePage";
import RepositoryFlakyTestsPage from "../FlakyTests/RepositoryFlakyTestsPage";
import RepositoryPerformanceTimelinePage from "../Performance/RepositoryPerformanceTimelinePage";

interface RepositoryHomePageProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const RepositoryHomePage = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryHomePageProps) => {
  return (
    <div>
      <RepositoryTimelinePage
        orgPart={orgPart}
        repoPart={repoPart}
        projectName={projectName}
        hideIfEmpty={true}
      />
      <RepositoryCoveragePage
        orgPart={orgPart}
        repoPart={repoPart}
        projectName={projectName}
        hideIfEmpty={true}
      />
      <RepositoryPerformanceTimelinePage
        orgPart={orgPart}
        repoPart={repoPart}
        projectName={projectName}
        hideIfEmpty={true}
      />
      <RepositoryFlakyTestsPage
        orgPart={orgPart}
        repoPart={repoPart}
        projectName={projectName}
        hideIfEmpty={true}
      />
    </div>
  );
};

export default RepositoryHomePage;
