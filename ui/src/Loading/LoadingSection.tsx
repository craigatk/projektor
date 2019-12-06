import * as React from "react";
import { makeStyles } from "@material-ui/core/styles";
import LinearProgress from "@material-ui/core/LinearProgress";
import LoadingState from "./LoadingState";
import { Paper, Typography } from "@material-ui/core";

const useStyles = makeStyles(theme => ({
  progress: {
    width: "90%",
    marginLeft: "5%"
  },
  errorPaper: {
    padding: "20px 40px",
    textAlign: "center",
    maxWidth: "600px",
    margin: "auto",
    border: "2px solid red"
  }
}));

interface LoadingSectionProps {
  loadingState: LoadingState;
  successComponent: any;
  errorComponent?: any;
}

const LoadingSection = ({
  loadingState,
  successComponent,
  errorComponent
}: LoadingSectionProps) => {
  const classes = useStyles({});

  if (loadingState === LoadingState.Success) {
    return successComponent;
  } else if (loadingState === LoadingState.Error) {
    return (
      errorComponent || (
        <div data-testid="loading-section-error">
          <Paper elevation={1} className={classes.errorPaper}>
            <Typography>
              Error loading data from server. Please{" "}
              <a href="javascript:window.location.reload()">refresh the page</a>{" "}
              and try again.
            </Typography>
          </Paper>
        </div>
      )
    );
  } else {
    return (
      <div data-testid="loading-section-progress">
        <LinearProgress className={classes.progress} />
      </div>
    );
  }
};

export default LoadingSection;
