import * as React from "react";
import { Link } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import {
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Typography
} from "@material-ui/core";
import { TestRunSummary } from "../model/TestRunModel";
import DashboardIcon from "../Icons/DashboardIcon";
import FailedIcon from "../Icons/FailedIcon";
import TotalIcon from "../Icons/TotalIcon";
import SlowIcon from "../Icons/SlowIcon";
import AttachmentIcon from "../Icons/AttachmentIcon";

interface TestRunSideMenuProps {
  publicId: string;
  testRunSummary: TestRunSummary;
}

const sideNavWidth = 180;

const useStyles = makeStyles(theme => ({
  drawer: {
    width: sideNavWidth,
    flexShrink: 0
  },
  drawerPaper: {
    width: sideNavWidth,
    //paddingTop: "20px",
    backgroundColor: "#1c313a",
    color: "white"
  },
  sideNavIcon: {
    minWidth: "40px",
    color: "white"
  },
  sideNavTitle: {
    textAlign: "center",
    fontSize: "1.5rem"
  },
  sideNavLink: {
    color: "white",
    textDecoration: "none"
  }
}));

const TestRunSideMenu = ({
  publicId,
  testRunSummary
}: TestRunSideMenuProps) => {
  const classes = useStyles({});

  return (
    <Drawer
      className={classes.drawer}
      variant="permanent"
      classes={{
        paper: classes.drawerPaper
      }}
      anchor="left"
    >
      <Typography variant="subtitle1" className={classes.sideNavTitle}>
        Projektor
      </Typography>
      <List>
        <Link to={`/tests/${publicId}/`} className={classes.sideNavLink}>
          <ListItem button>
            <ListItemIcon className={classes.sideNavIcon}>
              <DashboardIcon />
            </ListItemIcon>
            <ListItemText
              primary="Dashboard"
              data-testid="nav-link-dashboard"
            />
          </ListItem>
        </Link>
        {testRunSummary.passed === false ? (
          <Link
            to={`/tests/${publicId}/failed`}
            className={classes.sideNavLink}
          >
            <ListItem button>
              <ListItemIcon className={classes.sideNavIcon}>
                <FailedIcon />
              </ListItemIcon>
              <ListItemText
                primary="Failed tests"
                data-testid="nav-link-failed-test-cases"
              />
            </ListItem>
          </Link>
        ) : null}
        <Link to={`/tests/${publicId}/all`} className={classes.sideNavLink}>
          <ListItem button>
            <ListItemIcon className={classes.sideNavIcon}>
              <TotalIcon />
            </ListItemIcon>
            <ListItemText primary="All tests" data-testid="nav-link-all" />
          </ListItem>
        </Link>
        <Link to={`/tests/${publicId}/slow`} className={classes.sideNavLink}>
          <ListItem button>
            <ListItemIcon className={classes.sideNavIcon}>
              <SlowIcon />
            </ListItemIcon>
            <ListItemText primary="Slow tests" data-testid="nav-link-slow" />
          </ListItem>
        </Link>
        {testRunSummary.hasAttachments ? (
          <Link
            to={`/tests/${publicId}/attachments`}
            className={classes.sideNavLink}
          >
            <ListItem button>
              <ListItemIcon className={classes.sideNavIcon}>
                <AttachmentIcon />
              </ListItemIcon>
              <ListItemText
                primary="Attachments"
                data-testid="nav-link-attachments"
              />
            </ListItem>
          </Link>
        ) : null}
      </List>
    </Drawer>
  );
};

export default TestRunSideMenu;
