import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestOutputType from "../service/TestOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestSuiteSystemOutProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
}

const TestSuiteSystemOut = ({
  publicId,
  testSuiteIdx,
}: TestSuiteSystemOutProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      outputType={TestOutputType.SystemOut}
    />
  );
};

export default TestSuiteSystemOut;
