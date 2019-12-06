import * as React from "react";
import Typography from "@material-ui/core/Typography";
import { makeStyles } from "@material-ui/styles";

interface PageTitleProps {
  title: string;
  testid: string;
}

const useStyles = makeStyles(theme => ({
  title: {
    paddingTop: "10px",
    paddingLeft: "15px",
    paddingBottom: "10px",
    fontSize: "1.35em"
  }
}));

const PageTitle = ({ title, testid }: PageTitleProps) => {
  const classes = useStyles({});

  return (
    <Typography variant="h5" className={classes.title} data-testid={testid}>
      {title}
    </Typography>
  );
};

export default PageTitle;
