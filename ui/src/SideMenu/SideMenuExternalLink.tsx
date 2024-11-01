import * as React from "react";
import classes from "./SideMenuExternalLink.module.css";
import SideMenuItem from "./SideMenuItem";
import { Link } from "@material-ui/core";

interface SideMenuExternalLinkProps {
  linkTo: string;
  icon: any;
  linkText: string;
  linkTestId: string;
}

const SideMenuExternalLink = ({
  linkTo,
  icon,
  linkText,
  linkTestId,
}: SideMenuExternalLinkProps) => {
  return (
    <Link href={linkTo} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={linkText} testId={linkTestId} />
    </Link>
  );
};

export default SideMenuExternalLink;
