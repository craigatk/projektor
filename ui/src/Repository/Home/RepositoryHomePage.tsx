import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import RepositoryTimelinePage from "../Timeline/RepositoryTimelinePage";
import RepositoryCoveragePage from "../Coverage/RepositoryCoveragePage";
import RepositoryPerformanceTimelinePage from "../Performance/RepositoryPerformanceTimelinePage";
import classes from "./RepositoryHomePage.module.css";

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
      <div className={classes.repoSection}>
        <RepositoryTimelinePage
          orgPart={orgPart}
          repoPart={repoPart}
          projectName={projectName}
          hideIfEmpty={true}
        />
      </div>
      <div className={classes.repoSection}>
        <RepositoryCoveragePage
          orgPart={orgPart}
          repoPart={repoPart}
          projectName={projectName}
          hideIfEmpty={true}
        />
      </div>
      <div className={classes.repoSection}>
        <RepositoryPerformanceTimelinePage
          orgPart={orgPart}
          repoPart={repoPart}
          projectName={projectName}
          hideIfEmpty={true}
        />
      </div>
    </div>
  );
};

export default RepositoryHomePage;
