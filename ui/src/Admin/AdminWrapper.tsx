import * as React from "react";
import classes from "./AdminWrapper.module.css";
import { RouteComponentProps, Router } from "@reach/router";
import { AppBar, Typography } from "@material-ui/core";
import AdminSideMenu from "./AdminSideMenu";
import AdminFailuresPage from "./Failures/AdminFailuresPage";

interface AdminWrapperProps extends RouteComponentProps {}

const AdminWrapper = ({}: AdminWrapperProps) => {
  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <div className={classes.appBarLabel}>
          <Typography variant="subtitle1">Admin</Typography>
        </div>
      </AppBar>
      <AdminSideMenu />
      <main className={classes.content}>
        <Router>
          <AdminFailuresPage path="/" />
          <AdminFailuresPage path="/failures" />
        </Router>
      </main>
    </div>
  );
};

export default AdminWrapper;
