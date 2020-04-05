import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import { Drawer, List, Typography } from "@material-ui/core";
import { TestRunSummary } from "../model/TestRunModel";
import DashboardIcon from "../Icons/DashboardIcon";
import FailedIcon from "../Icons/FailedIcon";
import TotalIcon from "../Icons/TotalIcon";
import SlowIcon from "../Icons/SlowIcon";
import AttachmentIcon from "../Icons/AttachmentIcon";
import SideMenuLink from "./SideMenuLink";
import PinSideMenuItem from "../Pin/PinSideMenuItem";

interface SideMenuProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  hasAttachments: boolean;
}

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

const SideMenu = ({
  publicId,
  testRunSummary,
  hasAttachments,
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
        <PinSideMenuItem publicId={publicId} />
      </List>
    </Drawer>
  );
};

export default SideMenu;
