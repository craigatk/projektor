import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestCase } from "../../../model/TestRunModel";
import TestCaseList from "../TestCaseList";
import moment from "moment";

describe("TestCaseList", () => {
  it("should render duration column first when specified", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "Test Case",
      testSuiteName: "",
      packageName: "",
      className: "",
      fullName: "Test Case",
      duration: 1.2,
      passed: false,
      skipped: false,
      hasSystemOut: false,
      hasSystemErr: false,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: false,
      hasSystemErrTestSuite: false,
      failure: null,
      createdTimestamp: moment("2020-04-25").toDate(),
    };

    const { queryAllByRole } = render(
      <TestCaseList
        showDurationFirst={true}
        publicId="12345"
        testCases={[testCase]}
        showFullTestCaseName={true}
      />
    );

    const headerCells = queryAllByRole("rowheader");
    expect(headerCells.length).toBe(3);
    expect(headerCells[0].getAttribute("data-testid")).toContain(
      "test-list-duration-header"
    );
    expect(headerCells[1].getAttribute("data-testid")).toContain(
      "test-list-name-header"
    );
    expect(headerCells[2].getAttribute("data-testid")).toContain(
      "test-list-result-header"
    );

    const rowCells = queryAllByRole("rowcell");
    expect(rowCells.length).toBe(3);
    expect(rowCells[0].getAttribute("data-testid")).toContain(
      "test-case-duration"
    );
    expect(rowCells[1].getAttribute("data-testid")).toContain("test-case-name");
    expect(rowCells[2].getAttribute("data-testid")).toContain(
      "test-case-result"
    );
  });
});
