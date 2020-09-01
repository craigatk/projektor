import * as React from "react";
import { Link } from "@reach/router";
import { styled } from "@material-ui/core/styles";

interface CleanLinkProps {
  color?: string;
}

const CleanLink = styled(Link)((props: CleanLinkProps) => ({
  textDecoration: "none",
  color: props.color ? props.color : "blue",
  "&:hover": {
    textDecoration: "underline",
  },
  "&:visited": {
    color: props.color ? props.color : "blue",
  },
}));

export default CleanLink;
