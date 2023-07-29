import * as React from "react";
import { CodeQualityReports } from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import { fetchCodeQualityReports } from "../service/TestRunService";
import LoadingSection from "../Loading/LoadingSection";
import { RouteComponentProps } from "@reach/router";
import CodeQualityReportsSection from "./CodeQualityReportsSection";

interface CodeQualityReportsPageProps extends RouteComponentProps {
  publicId: string;
}

const CodeQualityReportsPage = ({ publicId }: CodeQualityReportsPageProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [codeQualityReports, setCodeQualityReports] =
    React.useState<CodeQualityReports>(null);

  React.useEffect(() => {
    fetchCodeQualityReports(publicId)
      .then((response) => {
        setCodeQualityReports(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setCodeQualityReports, setLoadingState]);

  return (
    <div>
      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <CodeQualityReportsSection
            codeQualityReports={codeQualityReports}
            publicId={publicId}
          />
        }
      />
    </div>
  );
};

export default CodeQualityReportsPage;
