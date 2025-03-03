import * as React from "react";

import { RouteComponentProps } from "@reach/router";
import classes from "./TestCaseFailureAnalysisSection.module.css";
import { TestCaseFailureAnalysis } from "../model/TestRunModel";
import { useEffect } from "react";
import { fetchTestCaseFailureAnalysis } from "../service/TestRunService";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";

interface TestCaseFailureAnalysisProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const TestCaseFailureAnalysisSection = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
}: TestCaseFailureAnalysisProps) => {
  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [analysis, setAnalysis] = React.useState<TestCaseFailureAnalysis>(null);

  useEffect(() => {
    fetchTestCaseFailureAnalysis(publicId, testSuiteIdx, testCaseIdx)
      .then((response) => {
        setAnalysis(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setAnalysis]);

  return (
    <LoadingSection
      loadingState={loadingState}
      successComponent={
        analysis && (
          <div className={classes.analysisContents}>
            <div>
              <pre
                data-testid="test-case-failure-analysis-text"
                className={classes.analysisText}
              >
                {analysis.analysis}
              </pre>
            </div>
          </div>
        )
      }
    />
  );
};

export default TestCaseFailureAnalysisSection;
