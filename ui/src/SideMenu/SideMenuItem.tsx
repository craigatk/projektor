import * as React from "react";
import classes from "./SideMenuItem.module.css";
import { ListItem, ListItemIcon, ListItemText } from "@mui/material";

interface SideMenuItemProps {
  icon: any;
  text: string;
  testId: string;
}

const SideMenuItem = ({ icon, text, testId }: SideMenuItemProps) => {
  return (
    <ListItem>
      <ListItemIcon className={classes.sideNavIcon}>{icon}</ListItemIcon>
      <ListItemText primary={text} data-testid={testId} />
    </ListItem>
  );
};

export default SideMenuItem;
