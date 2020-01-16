import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render, getNodeText } from "@testing-library/react";
import { TestSuite } from "../../model/TestRunModel";
import TestSuiteList from "../TestSuiteList";

describe("TestSuiteList", () => {
  it("should render group name when a test suite is grouped", () => {
    const testSuite1: TestSuite = {
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
      hasSystemErr: false,
      groupName: "Group1"
    };

    const testSuite2: TestSuite = {
      idx: 2,
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
      hasSystemErr: false,
      groupName: "Group2"
    };

    const { getByTestId, queryByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite1, testSuite2]} />
    );

    expect(getNodeText(getByTestId("test-suite-group-name-1"))).toBe("Group1");
    expect(getNodeText(getByTestId("test-suite-group-name-2"))).toBe("Group2");
  });

  it("should not display group name when test suite has no groups", () => {
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
      hasSystemErr: false,
      groupName: null
    };

    const { queryByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite]} />
    );

    expect(queryByTestId("test-suite-group-name-1")).toBeNull();
  });
});
