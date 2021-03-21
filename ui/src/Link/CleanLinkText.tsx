import * as React from "react";
import { styled } from "@material-ui/core/styles";
import { Link } from "@material-ui/core";

interface CleanLinkProps {
  color?: string;
}

const CleanLinkText = styled(Link)((props: CleanLinkProps) => ({
  textDecoration: "none",
  color: props.color ? props.color : "blue",
  "&:hover": {
    textDecoration: "underline",
    cursor: "pointer",
  },
  "&:visited": {
    color: props.color ? props.color : "blue",
  },
}));

export default CleanLinkText;
