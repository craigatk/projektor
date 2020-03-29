import * as React from "react";
import LoadingState from "../Loading/LoadingState";
import { TestRunSummary } from "../model/TestRunModel";
import {
  fetchAttachments,
  fetchTestRunSummary,
} from "../service/TestRunService";
import LoadingSection from "../Loading/LoadingSection";
import TestRunMenuWrapper from "./TestRunMenuWrapper";
import { RouteComponentProps } from "@reach/router";
import TestResultsProcessingCheck from "../TestResults/TestResultsProcessingCheck";

interface TestRunDataWrapperProps extends RouteComponentProps {
  publicId: string;
}

const TestRunDataWrapper = ({ publicId }: TestRunDataWrapperProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [testRunSummary, setTestRunSummary] = React.useState<TestRunSummary>(
    null
  );
  const [hasAttachments, setHasAttachments] = React.useState<boolean>(false);

  const loadTestRunSummary = () => {
    fetchTestRunSummary(publicId)
      .then((response) => {
        setTestRunSummary(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  };

  React.useEffect(loadTestRunSummary, [setTestRunSummary, setLoadingState]);
  React.useEffect(() => {
    fetchAttachments(publicId)
      .then((response) => {
        setHasAttachments(response.data.attachments.length > 0);
      })
      .catch(() => setHasAttachments(false));
  }, [setHasAttachments]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <TestRunMenuWrapper
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={hasAttachments}
        />
      }
      errorComponent={
        <TestResultsProcessingCheck
          publicId={publicId}
          processingSucceeded={loadTestRunSummary}
          refreshInterval={3000}
        />
      }
    />
  );
};

export default TestRunDataWrapper;
