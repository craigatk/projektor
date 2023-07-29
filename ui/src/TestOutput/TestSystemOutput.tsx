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
import styled from "styled-components";

interface TestSystemOutputProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx?: number;
  outputType: TestOutputType;
}

const OutputWrapper = styled.div`
  padding-top: 10px;
  padding-bottom: 10px;
  background-color: #EDEDED;
  border-radius: 8px;
  overflow-x: auto;
`

const TestSystemOutput = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
  outputType,
}: TestSystemOutputProps) => {
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
    <OutputWrapper>
      <LoadingSection
        loadingState={loadingState}
        successComponent={
          <CodeText text={output != null ? output.value : null} />
        }
      />
    </OutputWrapper>
  );
};

export default TestSystemOutput;
