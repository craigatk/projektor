import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import { TestCase, TestFailure } from "../../model/TestRunModel";
import TestCaseFailurePanelList from "../TestCaseFailurePanelList";

describe("TestCaseFailureListPanel", () => {
  it("should render longer failure text when less than 5 failures", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My longer and more descriptive failure text",
      failureType: "",
    };

    const testCases = [1, 2, 3, 4].map((idx) =>
      createTestCaseWithFailure(idx, failure),
    );

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />,
    );

    expect(getByTestId("test-case-failure-text-2-1")).toHaveTextContent(
      "My longer and more descriptive failure text",
    );
  });

  it("should render shorter failure message when more than 5 failures", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My longer and more descriptive failure text",
      failureType: "",
    };

    const testCases = [1, 2, 3, 4, 5, 6].map((idx) =>
      createTestCaseWithFailure(idx, failure),
    );

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />,
    );

    expect(getByTestId("test-case-failure-text-2-1")).toHaveTextContent(
      "My failure message",
    );
  });

  it("should expand all test failures when there are 15", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My longer and more descriptive failure text",
      failureType: "",
    };

    const testCases = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15].map(
      (idx) => createTestCaseWithFailure(idx, failure),
    );

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />,
    );

    expect(getByTestId("test-case-summary-2-15")).toHaveClass("Mui-expanded");
  });

  it("should not expand all test failures when there are 20", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My longer and more descriptive failure text",
      failureType: "",
    };

    const testCases = [
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
    ].map((idx) => createTestCaseWithFailure(idx, failure));

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />,
    );

    expect(getByTestId("test-case-summary-2-1")).not.toHaveClass(
      "Mui-expanded",
    );
  });

  function createTestCaseWithFailure(
    idx: number,
    failure: TestFailure,
  ): TestCase {
    return {
      idx,
      testSuiteIdx: 2,
      name: "Test Case",
      packageName: "",
      className: "",
      fullName: "Test Case",
      duration: 1.2,
      passed: false,
      skipped: false,
      hasSystemOut: true,
      hasSystemErr: true,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: true,
      hasSystemErrTestSuite: true,
      failure,
    } as TestCase;
  }
});
