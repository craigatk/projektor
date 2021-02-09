import * as React from "react";
import TestSystemOutput from "./TestSystemOutput";
import TestOutputType from "../service/TestOutputType";
import { RouteComponentProps } from "@reach/router";

interface TestCaseSystemErrProps extends RouteComponentProps {
  publicId: string;
  testSuiteIdx: number;
  testCaseIdx: number;
}

const TestCaseSystemErr = ({
  publicId,
  testSuiteIdx,
  testCaseIdx,
}: TestCaseSystemErrProps) => {
  return (
    <TestSystemOutput
      publicId={publicId}
      testSuiteIdx={testSuiteIdx}
      testCaseIdx={testCaseIdx}
      outputType={TestOutputType.SystemErr}
    />
  );
};

export default TestCaseSystemErr;
