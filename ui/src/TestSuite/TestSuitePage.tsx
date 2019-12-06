import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import LoadingState from "../Loading/LoadingState";
import { TestSuite } from "../model/TestRunModel";
import LoadingSection from "../Loading/LoadingSection";
import TestSuiteDetails from "./TestSuiteDetails";
import { fetchTestSuite } from "../service/TestRunService";

interface TestSuitePageProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
}

const TestSuitePage = ({ publicId, testSuiteIdx }: TestSuitePageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [testSuite, setTestSuite] = React.useState<TestSuite>(null);

  React.useEffect(() => {
    fetchTestSuite(publicId, testSuiteIdx)
      .then(response => {
        setTestSuite(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setTestSuite, setLoadingState]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <TestSuiteDetails publicId={publicId} testSuite={testSuite} />
      }
    />
  );
};

export default TestSuitePage;
