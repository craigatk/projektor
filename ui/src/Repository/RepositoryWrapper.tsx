import * as React from "react";
import classes from "./RepositoryWrapper.module.css";
import { globalHistory, RouteComponentProps, Router } from "@reach/router";
import { AppBar, Typography } from "@mui/material";
import RepositorySideMenu from "./RepositorySideMenu";
import RepositoryCoveragePage from "./Coverage/RepositoryCoveragePage";
import RepositoryTimelinePage from "./Timeline/RepositoryTimelinePage";
import RepositoryHomePage from "./Home/RepositoryHomePage";
import RepositoryFlakyTestsPage from "./FlakyTests/RepositoryFlakyTestsPage";
import RepositoryPerformanceTimelinePage from "./Performance/RepositoryPerformanceTimelinePage";
import { QueryParamProvider } from "use-query-params";

interface RepositoryWrapperProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const RepositoryWrapper = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryWrapperProps) => {
  const repoName = `${orgPart}/${repoPart}`;

  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <div className={classes.appBarLabel}>
          <Typography variant="subtitle1">
            {repoName} {projectName || ""}
          </Typography>
        </div>
      </AppBar>
      <RepositorySideMenu
        orgName={orgPart}
        repoName={repoName}
        projectName={projectName}
      />
      <main className={classes.content}>
        <QueryParamProvider reachHistory={globalHistory}>
          <Router>
            <RepositoryHomePage
              path="/"
              orgPart={orgPart}
              repoPart={repoPart}
              projectName={projectName}
            />
            <RepositoryCoveragePage
              path="/coverage"
              orgPart={orgPart}
              repoPart={repoPart}
              projectName={projectName}
              hideIfEmpty={false}
            />
            <RepositoryPerformanceTimelinePage
              path="/performance"
              orgPart={orgPart}
              repoPart={repoPart}
              projectName={projectName}
              hideIfEmpty={false}
            />
            <RepositoryTimelinePage
              path="/timeline"
              orgPart={orgPart}
              repoPart={repoPart}
              projectName={projectName}
              hideIfEmpty={false}
            />
            <RepositoryFlakyTestsPage
              path="/tests/flaky"
              orgPart={orgPart}
              repoPart={repoPart}
              projectName={projectName}
              hideIfEmpty={false}
            />
          </Router>
        </QueryParamProvider>
      </main>
    </div>
  );
};

export default RepositoryWrapper;
