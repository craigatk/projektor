import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import SideMenuItem from "./SideMenuItem";

interface SideMenuClickItemProps {
  onClick: () => void;
  icon: any;
  text: string;
  testId: string;
}

const useStyles = makeStyles((theme) => ({
  sideNavLink: {
    color: "white",
    "&:hover": {
      textDecoration: "underline",
    },
  },
}));

const SideMenuClickItem = ({
  onClick,
  icon,
  text,
  testId,
}: SideMenuClickItemProps) => {
  const classes = useStyles({});

  return (
    <div onClick={onClick} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={text} testId={testId} />
    </div>
  );
};

export default SideMenuClickItem;
