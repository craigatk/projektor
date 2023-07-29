import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import TestSuiteList from "../TestSuite/TestSuiteList";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import { fetchTestRun } from "../service/TestRunService";
import { TestRun } from "../model/TestRunModel";
import PageTitle from "../PageTitle";

interface TestRunAllTestsProps extends RouteComponentProps {
  publicId: string;
}

const TestRunAllTests = ({ publicId }: TestRunAllTestsProps) => {
  const [testRun, setTestRun] = React.useState<TestRun>(null);
  const [testRunLoadingState, setTestRunLoadingState] = React.useState(
    LoadingState.Loading,
  );

  React.useEffect(() => {
    fetchTestRun(publicId)
      .then((response) => {
        setTestRun(response.data);
        setTestRunLoadingState(LoadingState.Success);
      })
      .catch(() => setTestRunLoadingState(LoadingState.Error));
  }, [setTestRun, setTestRunLoadingState]);

  return (
    <div>
      <PageTitle title="All tests" testid="test-run-all-tests-title" />
      <LoadingSection
        loadingState={testRunLoadingState}
        successComponent={
          <TestSuiteList
            publicId={publicId}
            testSuites={testRun != null ? testRun.testSuites : []}
          />
        }
      />
    </div>
  );
};

export default TestRunAllTests;
