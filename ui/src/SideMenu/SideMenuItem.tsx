import * as React from "react";
import { ListItem, ListItemIcon, ListItemText } from "@material-ui/core";
import { Link } from "@reach/router";
import { makeStyles } from "@material-ui/styles";

interface SideMenuItemProps {
  linkTo: string;
  icon: any;
  linkText: string;
  linkTestId: string;
}

const useStyles = makeStyles(theme => ({
  sideNavIcon: {
    minWidth: "40px",
    color: "white"
  },
  sideNavLink: {
    color: "white",
    textDecoration: "none"
  }
}));

const SideMenuItem = ({
  linkTo,
  icon,
  linkText,
  linkTestId
}: SideMenuItemProps) => {
  const classes = useStyles({});

  return (
    <Link to={linkTo} className={classes.sideNavLink}>
      <ListItem button>
        <ListItemIcon className={classes.sideNavIcon}>{icon}</ListItemIcon>
        <ListItemText primary={linkText} data-testid={linkTestId} />
      </ListItem>
    </Link>
  );
};

export default SideMenuItem;
