import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import { Drawer, List, Typography } from "@material-ui/core";
import { TestRunGitMetadata, TestRunSummary } from "../model/TestRunModel";
import DashboardIcon from "../Icons/DashboardIcon";
import FailedIcon from "../Icons/FailedIcon";
import TotalIcon from "../Icons/TotalIcon";
import SlowIcon from "../Icons/SlowIcon";
import AttachmentIcon from "../Icons/AttachmentIcon";
import SideMenuLink from "./SideMenuLink";
import PinSideMenuItem from "../Pin/PinSideMenuItem";
import CoverageIcon from "../Icons/CoverageIcon";
import OrganizationIcon from "../Icons/OrganizationIcon";
import RepositoryIcon from "../Icons/RepositoryIcon";
import { repositoryLinkUrlUI } from "../Repository/RepositoryLink";

interface SideMenuProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  hasAttachments: boolean;
  hasCoverage: boolean;
  gitMetadata?: TestRunGitMetadata;
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

const SideMenu = ({
  publicId,
  testRunSummary,
  hasAttachments,
  hasCoverage,
  gitMetadata,
}: SideMenuProps) => {
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
          linkTo={`/tests/${publicId}/`}
          icon={<DashboardIcon />}
          linkText="Dashboard"
          linkTestId="nav-link-dashboard"
        />
        {testRunSummary.passed === false ? (
          <SideMenuLink
            linkTo={`/tests/${publicId}/failed`}
            icon={<FailedIcon />}
            linkText="Failed tests"
            linkTestId="nav-link-failed-test-cases"
          />
        ) : null}
        <SideMenuLink
          linkTo={`/tests/${publicId}/all`}
          icon={<TotalIcon />}
          linkText="All tests"
          linkTestId="nav-link-all"
        />
        {hasCoverage ? (
          <SideMenuLink
            linkTo={`/tests/${publicId}/coverage`}
            icon={<CoverageIcon />}
            linkText="Coverage"
            linkTestId="nav-link-coverage"
          />
        ) : null}
        <SideMenuLink
          linkTo={`/tests/${publicId}/slow`}
          icon={<SlowIcon />}
          linkText="Slow tests"
          linkTestId="nav-link-slow"
        />
        {hasAttachments ? (
          <SideMenuLink
            linkTo={`/tests/${publicId}/attachments`}
            icon={<AttachmentIcon />}
            linkText="Attachments"
            linkTestId="nav-link-attachments"
          />
        ) : null}
        {gitMetadata && gitMetadata.repoName && (
          <SideMenuLink
            linkTo={repositoryLinkUrlUI(
              gitMetadata.repoName,
              gitMetadata.projectName,
              null
            )}
            icon={<RepositoryIcon />}
            linkText="Repository"
            linkTestId="nav-link-repository"
          />
        )}
        {gitMetadata && gitMetadata.orgName && (
          <SideMenuLink
            linkTo={`/organization/${gitMetadata.orgName}`}
            icon={<OrganizationIcon />}
            linkText="Organization"
            linkTestId="nav-link-organization"
          />
        )}
        <PinSideMenuItem />
      </List>
    </Drawer>
  );
};

export default SideMenu;
