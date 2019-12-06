import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestSuite } from "../../model/TestRunModel";
import TestSuiteDetails from "../TestSuiteDetails";

describe("TestSuiteDetails", () => {
  it("should display system out tab if test suite has it", () => {
    const testSuite: TestSuite = {
      idx: 1,
      packageName: "",
      className: "",
      testCount: 1,
      passingCount: 1,
      skippedCount: 0,
      failureCount: 0,
      startTs: null,
      duration: 1.0,
      testCases: [],
      hasSystemOut: true,
      hasSystemErr: false
    };

    const { queryByTestId } = render(
      <TestSuiteDetails testSuite={testSuite} publicId="12345" />
    );

    expect(queryByTestId("test-suite-tab-system-out")).not.toBeNull();
    expect(queryByTestId("test-suite-tab-system-err")).toBeNull();
  });

  it("should display system err tab if test suite has it", () => {
    const testSuite: TestSuite = {
      idx: 1,
      packageName: "",
      className: "",
      testCount: 1,
      passingCount: 1,
      skippedCount: 0,
      failureCount: 0,
      startTs: null,
      duration: 1.0,
      testCases: [],
      hasSystemOut: false,
      hasSystemErr: true
    };

    const { queryByTestId } = render(
      <TestSuiteDetails testSuite={testSuite} publicId="12345" />
    );

    expect(queryByTestId("test-suite-tab-system-err")).not.toBeNull();
    expect(queryByTestId("test-suite-tab-system-out")).toBeNull();
  });
});
