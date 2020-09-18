import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import { Drawer, List, Typography } from "@material-ui/core";
import SideMenuLink from "../SideMenu/SideMenuLink";
import CoverageIcon from "../Icons/CoverageIcon";

interface RepositorySideMenuProps {
  repoName: string;
  projectName?: string;
}

const sideNavWidth = 180;

const useStyles = makeStyles(() => ({
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

const RepositorySideMenu = ({
  repoName,
  projectName,
}: RepositorySideMenuProps) => {
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
          linkTo={
            projectName
              ? `/repository/${repoName}/project/${projectName}/coverage`
              : `/repository/${repoName}/coverage`
          }
          icon={<CoverageIcon />}
          linkText="Coverage"
          linkTestId="nav-link-org-coverage"
        />
      </List>
    </Drawer>
  );
};

export default RepositorySideMenu;
