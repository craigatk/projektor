import * as React from "react";
import { RouteComponentProps, Router } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import { AppBar, Typography } from "@material-ui/core";
import AdminSideMenu from "./AdminSideMenu";
import AdminFailuresPage from "./Failures/AdminFailuresPage";

interface AdminWrapperProps extends RouteComponentProps {}

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

const AdminWrapper = ({}: AdminWrapperProps) => {
  const classes = useStyles({});

  return (
    <div className={classes.root} data-testid="organization-wrapper">
      <AppBar className={classes.appBar}>
        <Typography variant="subtitle1" className={classes.appBarLabel}>
          Admin
        </Typography>
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
