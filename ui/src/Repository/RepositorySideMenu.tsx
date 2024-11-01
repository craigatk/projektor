import * as React from "react";
import classes from "./RepositorySideMenu.module.css";
import { Drawer, List, Typography } from "@material-ui/core";
import SideMenuLink from "../SideMenu/SideMenuLink";
import CoverageIcon from "../Icons/CoverageIcon";
import OrganizationIcon from "../Icons/OrganizationIcon";
import { repositoryLinkUrlUI } from "./RepositoryLink";
import RepositoryIcon from "../Icons/RepositoryIcon";
import DashboardIcon from "../Icons/DashboardIcon";
import FlakyTestsIcon from "../Icons/FlakyTestsIcon";
import PerformanceIcon from "../Icons/PerformanceIcon";

interface RepositorySideMenuProps {
  repoName: string;
  orgName: string;
  projectName?: string;
}

const RepositorySideMenu = ({
  repoName,
  orgName,
  projectName,
}: RepositorySideMenuProps) => {
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
          linkTo={repositoryLinkUrlUI(repoName, projectName, "/")}
          icon={<DashboardIcon />}
          linkText="Dashboard"
          linkTestId="nav-link-repo-dashboard"
        />
        <SideMenuLink
          linkTo={repositoryLinkUrlUI(repoName, projectName, "/timeline")}
          icon={<RepositoryIcon />}
          linkText="Test duration"
          linkTestId="nav-link-repo-timeline"
        />
        <SideMenuLink
          linkTo={repositoryLinkUrlUI(repoName, projectName, "/coverage")}
          icon={<CoverageIcon />}
          linkText="Coverage"
          linkTestId="nav-link-repo-coverage"
        />
        <SideMenuLink
          linkTo={repositoryLinkUrlUI(repoName, projectName, "/performance")}
          icon={<PerformanceIcon />}
          linkText="Performance"
          linkTestId="nav-link-repo-performance"
        />
        <SideMenuLink
          linkTo={repositoryLinkUrlUI(repoName, projectName, "/tests/flaky")}
          icon={<FlakyTestsIcon />}
          linkText="Flaky tests"
          linkTestId="nav-link-repo-flaky-tests"
        />
        <SideMenuLink
          linkTo={`/organization/${orgName}`}
          icon={<OrganizationIcon />}
          linkText="Organization"
          linkTestId="nav-link-organization"
        />
      </List>
    </Drawer>
  );
};

export default RepositorySideMenu;
