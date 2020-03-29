import * as React from "react";
import { Link } from "@reach/router";
import { styled } from "@material-ui/core/styles";

const CleanLink = styled(Link)({
  textDecoration: "none",
  color: "blue",
  "&:hover": {
    textDecoration: "underline",
  },
  "&:visited": {
    color: "blue",
  },
});

export default CleanLink;
