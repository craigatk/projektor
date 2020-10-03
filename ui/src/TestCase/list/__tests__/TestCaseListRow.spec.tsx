import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestCase } from "../../../model/TestRunModel";
import TestCaseListRow from "../TestCaseListRow";

describe("TestCaseListRow", () => {
  it("should render test case list row", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      name: "this is a test method",
      packageName: "dev.projektor",
      className: "dev.projektor.TestCase",
      fullName: "Test Case",
      duration: 123.123,
      passed: false,
      skipped: false,
      hasSystemOut: false,
      hasSystemErr: false,
      failure: null,
    };
    const publicId = "12345";

    const { getByTestId } = render(
      <TestCaseListRow
        publicId={publicId}
        testCase={testCase}
        showDurationFirst={false}
        showFullTestCaseName={true}
      />
    );

    expect(getByTestId("test-case-name-link-2-1")).toHaveTextContent(
      "dev.projektor.dev.projektor.TestCase.this is a test method"
    );

    expect(getByTestId("test-case-duration-2-1")).toHaveTextContent(
      "2m 3.123s"
    );
  });
});
