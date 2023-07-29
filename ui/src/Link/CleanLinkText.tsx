import * as React from "react";
import { styled } from "@mui/material/styles";
import { Link } from "@mui/material";

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
