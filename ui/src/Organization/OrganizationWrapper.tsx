import * as React from "react";
import classes from "./OrganizationWrapper.module.css";
import { RouteComponentProps, Router } from "@reach/router";
import { AppBar, Typography } from "@mui/material";
import OrganizationSideMenu from "./OrganizationSideMenu";
import OrganizationCoveragePage from "./Coverage/OrganizationCoveragePage";

interface OrganizationWrapperProps extends RouteComponentProps {
  orgName: string;
}

const OrganizationWrapper = ({ orgName }: OrganizationWrapperProps) => {
  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <div className={classes.appBarLabel}>
          <Typography variant="subtitle1">{orgName}</Typography>
        </div>
      </AppBar>
      <OrganizationSideMenu orgName={orgName} />
      <main className={classes.content}>
        <Router>
          <OrganizationCoveragePage path="/" orgName={orgName} />
          <OrganizationCoveragePage path="/coverage" orgName={orgName} />
        </Router>
      </main>
    </div>
  );
};

export default OrganizationWrapper;
