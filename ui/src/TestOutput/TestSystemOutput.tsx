import * as React from "react";
import TestOutputType from "../service/TestOutputType";
import {
  fetchTestCaseSystemOutput,
  fetchTestSuiteSystemOutput,
} from "../service/TestRunService";
import { TestOutput } from "../model/TestRunModel";
import LoadingState from "../Loading/LoadingState";
import LoadingSection from "../Loading/LoadingSection";
import CodeText from "../CodeText/CodeText";
import { makeStyles } from "@material-ui/core/styles";

interface TestSystemOutputProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx?: number;
  outputType: TestOutputType;
}

const useStyles = makeStyles({
  paper: {
    paddingTop: "10px",
    paddingBottom: "10px",
    backgroundColor: "#EDEDED",
    borderRadius: "8px",
    overflowX: "auto",
  },
});

const TestSystemOutput = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
  outputType,
}: TestSystemOutputProps) => {
  const classes = useStyles({});

  const [loadingState, setLoadingState] = React.useState<LoadingState>(
    LoadingState.Loading,
  );
  const [output, setOutput] = React.useState<TestOutput>(null);

  React.useEffect(() => {
    if (testCaseIdx) {
      fetchTestCaseSystemOutput(publicId, testSuiteIdx, testCaseIdx, outputType)
        .then((response) => {
          setOutput(response.data);
          setLoadingState(LoadingState.Success);
        })
        .catch(() => setLoadingState(LoadingState.Error));
    } else {
      fetchTestSuiteSystemOutput(publicId, testSuiteIdx, outputType)
        .then((response) => {
          setOutput(response.data);
          setLoadingState(LoadingState.Success);
        })
        .catch(() => setLoadingState(LoadingState.Error));
    }
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
