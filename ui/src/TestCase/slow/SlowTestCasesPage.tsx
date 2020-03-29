import * as React from "react";
import { RouteComponentProps, Link } from "@reach/router";
import { TestCase } from "../../model/TestRunModel";
import { fetchSlowTestCases } from "../../service/TestRunService";
import LoadingState from "../../Loading/LoadingState";
import LoadingSection from "../../Loading/LoadingSection";
import SlowTestCasesDetails from "./SlowTestCasesDetails";

interface SlowTestCasesPageProps extends RouteComponentProps {
  publicId: string;
}

const SlowTestCasesPage = ({ publicId }: SlowTestCasesPageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [testCases, setTestCases] = React.useState<TestCase[]>([]);

  React.useEffect(() => {
    fetchSlowTestCases(publicId).then((response) => {
      setTestCases(response.data);
      setLoadingState(LoadingState.Success);
    });
  }, [setTestCases, setLoadingState]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        <SlowTestCasesDetails publicId={publicId} testCases={testCases} />
      }
    />
  );
};

export default SlowTestCasesPage;
