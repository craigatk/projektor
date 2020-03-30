import * as React from "react";
import { Link } from "@reach/router";
import { makeStyles } from "@material-ui/styles";
import SideMenuItem from "./SideMenuItem";

interface SideMenuLinkProps {
  linkTo: string;
  icon: any;
  linkText: string;
  linkTestId: string;
}

const useStyles = makeStyles((theme) => ({
  sideNavLink: {
    color: "white",
    textDecoration: "none",
  },
}));

const SideMenuLink = ({
  linkTo,
  icon,
  linkText,
  linkTestId,
}: SideMenuLinkProps) => {
  const classes = useStyles({});

  return (
    <Link to={linkTo} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={linkText} testId={linkTestId} />
    </Link>
  );
};

export default SideMenuLink;
