import * as React from "react";
import { ListItem, ListItemIcon, ListItemText } from "@mui/material";
import { makeStyles } from "@material-ui/styles";

interface SideMenuItemProps {
  icon: any;
  text: string;
  testId: string;
}

const useStyles = makeStyles(() => ({
  sideNavIcon: {
    minWidth: "40px",
    color: "white",
  },
}));

const SideMenuItem = ({ icon, text, testId }: SideMenuItemProps) => {
  const classes = useStyles({});

  return (
    <ListItem button>
      <ListItemIcon className={classes.sideNavIcon}>{icon}</ListItemIcon>
      <ListItemText primary={text} data-testid={testId} />
    </ListItem>
  );
};

export default SideMenuItem;
