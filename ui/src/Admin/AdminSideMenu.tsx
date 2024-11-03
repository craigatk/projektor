import * as React from "react";
import classes from "./AdminSideMenu.module.css";
import { Drawer, List, Typography } from "@mui/material";
import SideMenuLink from "../SideMenu/SideMenuLink";
import FailedIcon from "../Icons/FailedIcon";

const AdminSideMenu = () => {
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
