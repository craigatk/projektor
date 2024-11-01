import * as React from "react";
import classes from "./SideMenuLink.module.css";
import { Link } from "@reach/router";
import SideMenuItem from "./SideMenuItem";

interface SideMenuLinkProps {
  linkTo: string;
  icon: any;
  linkText: string;
  linkTestId: string;
}

const SideMenuLink = ({
  linkTo,
  icon,
  linkText,
  linkTestId,
}: SideMenuLinkProps) => {
  return (
    <Link to={linkTo} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={linkText} testId={linkTestId} />
    </Link>
  );
};

export default SideMenuLink;
