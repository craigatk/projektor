import * as React from "react";
import classes from "./SideMenu.module.css";
import { Drawer, List, Typography } from "@mui/material";
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
import CodeQualityIcon from "../Icons/CodeQualityIcon";
import SideMenuExternalLink from "./SideMenuExternalLink";
import DocsIcon from "../Icons/DocsIcon";

interface SideMenuProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  hasAttachments: boolean;
  hasCoverage: boolean;
  gitMetadata?: TestRunGitMetadata;
}

const SideMenu = ({
  publicId,
  testRunSummary,
  hasAttachments,
  hasCoverage,
  gitMetadata,
}: SideMenuProps) => {
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
        <SideMenuLink
          linkTo={`/tests/${publicId}/quality`}
          icon={<CodeQualityIcon />}
          linkText="Code quality"
          linkTestId="nav-link-code-quality"
        />
        {gitMetadata && gitMetadata.repoName && (
          <SideMenuLink
            linkTo={repositoryLinkUrlUI(
              gitMetadata.repoName,
              gitMetadata.projectName,
              null,
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
        <SideMenuExternalLink
          linkTo="https://projektor.dev/docs/"
          icon={<DocsIcon />}
          linkText="Docs"
          linkTestId="nav-link-docs"
        />
      </List>
    </Drawer>
  );
};

export default SideMenu;
