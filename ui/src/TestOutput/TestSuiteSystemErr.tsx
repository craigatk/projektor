import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestOutputType from "../service/TestOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestSuiteSystemErrProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
}

const TestSuiteSystemErr = ({
  publicId,
  testSuiteIdx,
}: TestSuiteSystemErrProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      outputType={TestOutputType.SystemErr}
    />
  );
};

export default TestSuiteSystemErr;
