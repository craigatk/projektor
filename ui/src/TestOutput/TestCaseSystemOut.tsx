import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestOutputType from "../service/TestOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestCaseSystemOutProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const TestCaseSystemOut = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
}: TestCaseSystemOutProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      testCaseIdx={testCaseIdx}
      outputType={TestOutputType.SystemOut}
    />
  );
};

export default TestCaseSystemOut;
