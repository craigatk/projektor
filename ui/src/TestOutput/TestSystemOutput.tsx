import * as React from "react";
import TestSuiteOutputType from "../service/TestSuiteOutputType";
import { fetchTestSuiteSystemOutput } from "../service/TestRunService";
import { TestSuiteOutput } from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import CodeText from "../CodeText/CodeText";
import { makeStyles } from "@material-ui/core/styles";

interface TestSystemOutputProps {
  publicId: String;
  testSuiteIdx: number;
  outputType: TestSuiteOutputType;
}

const useStyles = makeStyles({
  paper: {
    paddingTop: "10px",
    paddingBottom: "10px",
    backgroundColor: "#EDEDED",
    borderRadius: "8px",
    overflowX: "auto"
  }
});

const TestSystemOutput = ({
  publicId,
  testSuiteIdx,
  outputType
}: TestSystemOutputProps) => {
  const classes = useStyles({});

  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading
  );
  const [output, setOutput] = React.useState<TestSuiteOutput>(null);

  React.useEffect(() => {
    fetchTestSuiteSystemOutput(publicId, testSuiteIdx, outputType)
      .then(response => {
        setOutput(response.data);
        setLoadingState(LoadingState.Success);
      })
      .catch(() => setLoadingState(LoadingState.Error));
  }, [setOutput, setLoadingState]);

  return (
    <div className={classes.paper}>
      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <CodeText text={output != null ? output.value : null} />
        }
      />
    </div>
  );
};

export default TestSystemOutput;
