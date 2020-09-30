import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { getNodeText, render } from "@testing-library/react";
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
      createTestCaseWithFailure(idx, failure)
    );

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />
    );

    expect(getNodeText(getByTestId("test-case-failure-text-2-1"))).toContain(
      "My longer and more descriptive failure text"
    );
  });

  it("should render shorter failure message when more than 5 failures", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My longer and more descriptive failure text",
      failureType: "",
    };

    const testCases = [1, 2, 3, 4, 5, 6].map((idx) =>
      createTestCaseWithFailure(idx, failure)
    );

    const { getByTestId } = render(
      <TestCaseFailurePanelList failedTestCases={testCases} publicId="12345" />
    );

    expect(getNodeText(getByTestId("test-case-failure-text-2-1"))).toContain(
      "My failure message"
    );
  });

  function createTestCaseWithFailure(
    idx: number,
    failure: TestFailure
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
      failure,
    } as TestCase;
  }
});
