import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import { Drawer, List, Typography } from "@material-ui/core";
import SideMenuLink from "../SideMenu/SideMenuLink";
import FailedIcon from "../Icons/FailedIcon";

const sideNavWidth = 180;

const useStyles = makeStyles((theme) => ({
  drawer: {
    width: sideNavWidth,
    flexShrink: 0,
  },
  drawerPaper: {
    width: sideNavWidth,
    backgroundColor: "#1c313a",
    color: "white",
  },
  sideNavTitle: {
    textAlign: "center",
    fontSize: "1.5rem",
  },
}));

const AdminSideMenu = () => {
  const classes = useStyles({});

  return (
    <Drawer
      className={classes.drawer}
      variant="permanent"
      classes={{
        paper: classes.drawerPaper,
      }}
      anchor="left"
    >
      <Typography variant="subtitle1" className={classes.sideNavTitle}>
        Projektor
      </Typography>
      <List>
        <SideMenuLink
          linkTo={`/admin/failures`}
          icon={<FailedIcon />}
          linkText="Errors"
          linkTestId="nav-link-admin-failures"
        />
      </List>
    </Drawer>
  );
};

export default AdminSideMenu;
