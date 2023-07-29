import * as React from "react";
import { RouteComponentProps, Router } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import { AppBar, Typography } from "@mui/material";
import OrganizationSideMenu from "./OrganizationSideMenu";
import OrganizationCoveragePage from "./Coverage/OrganizationCoveragePage";

interface OrganizationWrapperProps extends RouteComponentProps {
  orgName: string;
}

const useStyles = makeStyles((theme) => ({
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

const OrganizationWrapper = ({ orgName }: OrganizationWrapperProps) => {
  const classes = useStyles({});

  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <Typography variant="subtitle1" className={classes.appBarLabel}>
          {orgName}
        </Typography>
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
