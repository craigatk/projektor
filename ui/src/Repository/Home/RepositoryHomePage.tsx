import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import RepositoryTimelinePage from "../Timeline/RepositoryTimelinePage";
import RepositoryCoveragePage from "../Coverage/RepositoryCoveragePage";

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
      />
      <RepositoryCoveragePage
        orgPart={orgPart}
        repoPart={repoPart}
        projectName={projectName}
      />
    </div>
  );
};

export default RepositoryHomePage;
