import * as React from "react";
import classes from "./OrganizationSideMenu.module.css";
import { Drawer, List, Typography } from "@mui/material";
import SideMenuLink from "../SideMenu/SideMenuLink";
import CoverageIcon from "../Icons/CoverageIcon";

interface OrganizationSideMenuProps {
  orgName: string;
}

const OrganizationSideMenu = ({ orgName }: OrganizationSideMenuProps) => {
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
          linkTo={`/organization/${orgName}/coverage`}
          icon={<CoverageIcon />}
          linkText="Coverage"
          linkTestId="nav-link-org-coverage"
        />
      </List>
    </Drawer>
  );
};

export default OrganizationSideMenu;
