import * as React from "react";
import { TestCase } from "../model/TestRunModel";
import SkippedIcon from "../Icons/SkippedIcon";
import PassedIcon from "../Icons/PassedIcon";
import FailedIcon from "../Icons/FailedIcon";

interface TestCaseResultIconProps {
  testCase: TestCase;
}

const TestCaseResultIcon = ({ testCase }: TestCaseResultIconProps) => {
  if (testCase.skipped) {
    return <SkippedIcon />;
  } else if (testCase.passed) {
    return <PassedIcon />;
  } else {
    return <FailedIcon />;
  }
};

export default TestCaseResultIcon;
