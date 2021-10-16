import * as React from "react";
import { PerformanceResults } from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import { fetchPerformanceResults } from "../service/TestRunService";
import LoadingSection from "../Loading/LoadingSection";
import PerformanceDetails from "./PerformanceDetails";

interface PerformanceSectionProps {
  publicId: string;
}

const PerformanceSection = ({ publicId }: PerformanceSectionProps) => {
  const [performanceResults, setPerformanceResults] =
    React.useState<PerformanceResults>(null);
  const [testRunLoadingState, setTestRunLoadingState] = React.useState(
    LoadingState.Loading
  );

  React.useEffect(() => {
    fetchPerformanceResults(publicId)
      .then((response) => {
        setPerformanceResults(response.data);
        setTestRunLoadingState(LoadingState.Success);
      })
      .catch(() => setTestRunLoadingState(LoadingState.Error));
  }, [setPerformanceResults, setTestRunLoadingState]);

  return (
    <div>
      <LoadingSection
        loadingState={testRunLoadingState}
        successComponent={
          <PerformanceDetails
            performanceResults={
              performanceResults != null ? performanceResults.results : []
            }
          />
        }
      />
    </div>
  );
};

export default PerformanceSection;
