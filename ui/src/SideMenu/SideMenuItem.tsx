import * as React from "react";
import classes from "./SideMenuItem.module.css";
import { ListItem, ListItemIcon, ListItemText } from "@material-ui/core";

interface SideMenuItemProps {
  icon: any;
  text: string;
  testId: string;
}

const SideMenuItem = ({ icon, text, testId }: SideMenuItemProps) => {
  return (
    <ListItem button>
      <ListItemIcon className={classes.sideNavIcon}>{icon}</ListItemIcon>
      <ListItemText primary={text} data-testid={testId} />
    </ListItem>
  );
};

export default SideMenuItem;
