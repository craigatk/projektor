import * as React from "react";
import classes from "./TestResultsProcessingCheck.module.css";
import {
  TestResultsProcessing,
  TestResultsProcessingStatus,
} from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import { Paper, Typography } from "@material-ui/core";
import LinearProgress from "@material-ui/core/LinearProgress";
import { fetchTestResultsProcessing } from "../service/TestRunService";
import { RouteComponentProps } from "@reach/router";

interface TestResultsProcessingCheckProps extends RouteComponentProps {
  publicId: string;
  processingSucceeded: () => void;
  refreshInterval: number;
  autoRefreshTimeout: number;
}

const resultsAreStillProcessing = (
  processing: TestResultsProcessing,
): boolean => {
  return (
    !processing ||
    processing.status === TestResultsProcessingStatus.RECEIVED ||
    processing.status === TestResultsProcessingStatus.PROCESSING
  );
};

const TestResultsProcessingCheck = ({
  publicId,
  processingSucceeded,
  refreshInterval,
  autoRefreshTimeout,
}: TestResultsProcessingCheckProps) => {
  let totalWaitTime = 0;

  const [resultsProcessing, setResultsProcessing] =
    React.useState<TestResultsProcessing>(null);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );

  const loadTestResultsProcessing = () => {
    fetchTestResultsProcessing(publicId)
      .then((response) => {
        setResultsProcessing(response.data);
        setLoadingState(LoadingState.Success);

        if (
          resultsAreStillProcessing(response.data) &&
          totalWaitTime < autoRefreshTimeout
        ) {
          setTimeout(loadTestResultsProcessing, refreshInterval);
          totalWaitTime += refreshInterval;
        }
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(loadTestResultsProcessing, [
    setResultsProcessing,
    setLoadingState,
  ]);

  const resultsStillProcessing = resultsAreStillProcessing(resultsProcessing);

  const resultsProcessingSuccessful =
    resultsProcessing &&
    resultsProcessing.status === TestResultsProcessingStatus.SUCCESS;
  const resultsProcessingFailed =
    resultsProcessing &&
    resultsProcessing.status === TestResultsProcessingStatus.ERROR;
  const resultsDeleted =
    resultsProcessing &&
    resultsProcessing.status === TestResultsProcessingStatus.DELETED;

  if (resultsProcessingSuccessful) {
    processingSucceeded();
    return null;
  } else if (resultsStillProcessing) {
    return (
      <Paper
        elevation={2}
        className={classes.paper}
        data-testid="results-still-processing"
      >
        <Typography>
          Your test results are still processing, please wait a few moments
        </Typography>
        <LinearProgress className={classes.progress} />
      </Paper>
    );
  } else if (resultsProcessingFailed) {
    return (
      <Paper
        elevation={2}
        className={classes.paper}
        data-testid="results-processing-failed"
      >
        <Typography>Error processing test results</Typography>
        <pre className={classes.errorMessage}>
          {resultsProcessing.errorMessage}
        </pre>
      </Paper>
    );
  } else if (resultsDeleted) {
    return (
      <Paper
        elevation={2}
        className={classes.paper}
        data-testid="results-deleted"
      >
        <Typography>Test results cleaned up to save disk space</Typography>
      </Paper>
    );
  } else {
    return null;
  }
};

export default TestResultsProcessingCheck;
