import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import { TestCase } from "../model/TestRunModel";
import { fetchTestCaseDetails } from "../service/TestRunService";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import TestCaseDetails from "./TestCaseDetails";

interface TestCasePageProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const TestCasePage = ({
  publicId,
  testSuiteIdx,
  testCaseIdx
}: TestCasePageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [testCase, setTestCase] = React.useState<TestCase>(null);

  React.useEffect(() => {
    fetchTestCaseDetails(publicId, testSuiteIdx, testCaseIdx)
      .then(response => {
        setTestCase(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setTestCase, setLoadingState]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <TestCaseDetails publicId={publicId} testCase={testCase} />
      }
    />
  );
};

export default TestCasePage;
