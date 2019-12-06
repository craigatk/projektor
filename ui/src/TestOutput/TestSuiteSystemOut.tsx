import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestSuiteOutputType from "../service/TestSuiteOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestSuiteSystemOutProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
}

const TestSuiteSystemOut = ({
  publicId,
  testSuiteIdx
}: TestSuiteSystemOutProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      outputType={TestSuiteOutputType.SystemOut}
    />
  );
};

export default TestSuiteSystemOut;
