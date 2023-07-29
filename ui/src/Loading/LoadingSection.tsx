import * as React from "react";
import { makeStyles } from "@mui/material/styles";
import LinearProgress from "@mui/material/LinearProgress";
import LoadingState from "./LoadingState";
import { Paper, Typography } from "@mui/material";
import CleanLink from "../Link/CleanLink";

const useStyles = makeStyles((theme) => ({
  progress: {
    width: "90%",
    marginLeft: "5%",
  },
  errorPaper: {
    padding: "20px 40px",
    textAlign: "center",
    maxWidth: "600px",
    margin: "auto",
    border: "2px solid red",
  },
}));

interface LoadingSectionProps {
  loadingState: LoadingState;
  successComponent: any;
  errorComponent?: any;
}

const reloadPage = () => {
  window.location.reload();
};

const LoadingSection = ({
  loadingState,
  successComponent,
  errorComponent,
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
              <CleanLink to="" onClick={reloadPage}>
                refresh the page
              </CleanLink>{" "}
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
