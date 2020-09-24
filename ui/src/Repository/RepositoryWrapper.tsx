import * as React from "react";
import { RouteComponentProps, Router } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import { AppBar, Typography } from "@material-ui/core";
import RepositorySideMenu from "./RepositorySideMenu";
import RepositoryCoveragePage from "./Coverage/RepositoryCoveragePage";
import RepositoryTimelinePage from "./Timeline/RepositoryTimelinePage";
import RepositoryHomePage from "./Home/RepositoryHomePage";

interface RepositoryWrapperProps extends RouteComponentProps {
  orgPart: string;
  repoPart: string;
  projectName?: string;
}

const useStyles = makeStyles(() => ({
  root: {
    display: "flex",
  },
  appBar: {
    backgroundColor: "#1c313a",
    padding: "5px 10px",
    height: "42px",
  },
  appBarLabel: {
    marginLeft: "192px",
  },
  content: {
    flexGrow: 1,
    marginTop: "42px",
    maxWidth: "calc(100% - 180px)",
  },
}));

const RepositoryWrapper = ({
  orgPart,
  repoPart,
  projectName,
}: RepositoryWrapperProps) => {
  const classes = useStyles({});

  const repoName = `${orgPart}/${repoPart}`;

  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <Typography variant="subtitle1" className={classes.appBarLabel}>
          {repoName} {projectName || ""}
        </Typography>
      </AppBar>
      <RepositorySideMenu
        orgName={orgPart}
        repoName={repoName}
        projectName={projectName}
      />
      <main className={classes.content}>
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
          />
          <RepositoryTimelinePage
            path="/timeline"
            orgPart={orgPart}
            repoPart={repoPart}
            projectName={projectName}
          />
        </Router>
      </main>
    </div>
  );
};

export default RepositoryWrapper;
