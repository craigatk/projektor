import * as React from "react";
import Typography from "@mui/material/Typography";
import classes from "./PageTitle.module.css";

interface PageTitleProps {
  title: string;
  testid: string;
}

const PageTitle = ({ title, testid }: PageTitleProps) => {
  return (
    <Typography variant="h5" className={classes.title} data-testid={testid}>
      {title}
    </Typography>
  );
};

export default PageTitle;
