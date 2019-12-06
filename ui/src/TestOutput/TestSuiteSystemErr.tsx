import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestSuiteOutputType from "../service/TestSuiteOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestSuiteSystemErrProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
}

const TestSuiteSystemErr = ({
  publicId,
  testSuiteIdx
}: TestSuiteSystemErrProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      outputType={TestSuiteOutputType.SystemErr}
    />
  );
};

export default TestSuiteSystemErr;
