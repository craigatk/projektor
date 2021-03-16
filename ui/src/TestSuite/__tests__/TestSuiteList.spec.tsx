import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
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
      groupName: "Group1",
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
      groupName: "Group2",
    };

    const { getByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite1, testSuite2]} />
    );

    expect(getByTestId("test-suite-group-name-1")).toHaveTextContent("Group1");
    expect(getByTestId("test-suite-group-name-2")).toHaveTextContent("Group2");
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
      groupName: null,
    };

    const { queryByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite]} />
    );

    expect(queryByTestId("test-suite-group-name-1")).toBeNull();
  });

  it("should display file name column when suite has a file name set", () => {
    const testSuite: TestSuite = {
      idx: 1,
      fileName: "path/to/file.js",
      packageName: "",
      className: "my test suite",
      testCount: 1,
      passingCount: 1,
      skippedCount: 0,
      failureCount: 0,
      startTs: null,
      duration: 1.0,
      testCases: [],
      hasSystemOut: true,
      hasSystemErr: false,
      groupName: "Group1",
    };

    const { getByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite]} />
    );

    expect(getByTestId("test-suite-file-name-1")).toHaveTextContent(
      "path/to/file.js"
    );
    expect(getByTestId("test-suite-class-name-1")).toHaveTextContent(
      "my test suite"
    );
  });

  it("should not display file name column when no suite has a file name set", () => {
    const testSuite: TestSuite = {
      idx: 1,
      fileName: null,
      packageName: "",
      className: "my test suite",
      testCount: 1,
      passingCount: 1,
      skippedCount: 0,
      failureCount: 0,
      startTs: null,
      duration: 1.0,
      testCases: [],
      hasSystemOut: true,
      hasSystemErr: false,
      groupName: "Group1",
    };

    const { getByTestId, queryByTestId } = render(
      <TestSuiteList publicId="12345" testSuites={[testSuite]} />
    );

    expect(queryByTestId("test-suite-file-name-1")).toBeNull();
    expect(getByTestId("test-suite-class-name-1")).toHaveTextContent(
      "my test suite"
    );
  });
});
