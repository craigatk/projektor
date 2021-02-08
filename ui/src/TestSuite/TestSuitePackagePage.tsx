import * as React from "react";
import { RouteComponentProps } from "@reach/router";
import { TestSuite } from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import { fetchTestSuitesInPackage } from "../service/TestRunService";
import TestSuitePackageDetails from "./TestSuitePackageDetails";
import { useQueryParam, StringParam } from "use-query-params";

interface TestSuitePackagePageProps extends RouteComponentProps {
  publicId: string;
}

const TestSuitePackagePage = ({ publicId }: TestSuitePackagePageProps) => {
  const [packageName, setPackageName] = useQueryParam("name", StringParam);
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [testSuites, setTestSuites] = React.useState<TestSuite[]>([]);

  React.useEffect(() => {
    fetchTestSuitesInPackage(publicId, packageName)
      .then((response) => {
        setTestSuites(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setTestSuites, setLoadingState]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <TestSuitePackageDetails
          publicId={publicId}
          testSuiteSummaries={testSuites}
          packageName={packageName}
        />
      }
    />
  );
};

export default TestSuitePackagePage;
