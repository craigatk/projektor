import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import SideMenuItem from "./SideMenuItem";
import { Link } from "@mui/material";

interface SideMenuExternalLinkProps {
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

const SideMenuExternalLink = ({
  linkTo,
  icon,
  linkText,
  linkTestId,
}: SideMenuExternalLinkProps) => {
  const classes = useStyles({});

  return (
    <Link href={linkTo} className={classes.sideNavLink}>
      <SideMenuItem icon={icon} text={linkText} testId={linkTestId} />
    </Link>
  );
};

export default SideMenuExternalLink;
