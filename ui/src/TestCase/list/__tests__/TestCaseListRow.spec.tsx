import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestCase } from "../../../model/TestRunModel";
import TestCaseListRow from "../TestCaseListRow";
import moment from "moment";

describe("TestCaseListRow", () => {
  it("should render test case list row", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "this is a test method",
      packageName: "dev.projektor",
      className: "dev.projektor.TestCase",
      fullName: "Test Case",
      duration: 123.123,
      passed: false,
      skipped: false,
      hasSystemOut: false,
      hasSystemErr: false,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: false,
      hasSystemErrTestSuite: false,
      createdTimestamp: moment("2020-04-25").toDate(),
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
