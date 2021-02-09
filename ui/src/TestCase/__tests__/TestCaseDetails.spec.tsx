import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestCase } from "../../model/TestRunModel";
import TestCaseDetails from "../TestCaseDetails";
import moment from "moment";

describe("TestCaseDetails", () => {
  it("should render failure tab when the test case failed", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "Test Case",
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

    const { queryByTestId } = render(
      <TestCaseDetails testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-tab-summary")).not.toBeNull();
    expect(queryByTestId("test-case-tab-failure")).not.toBeNull();

    expect(queryByTestId("test-case-tab-system-out")).toBeNull();
    expect(queryByTestId("test-case-tab-system-err")).toBeNull();
  });

  it("should not render failure tab when the test case passed", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "Test Case",
      packageName: "",
      className: "",
      fullName: "Test Case",
      duration: 1.2,
      passed: true,
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

    const { queryByTestId } = render(
      <TestCaseDetails testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-tab-summary")).not.toBeNull();

    expect(queryByTestId("test-case-tab-failure")).toBeNull();
    expect(queryByTestId("test-case-tab-system-out")).toBeNull();
    expect(queryByTestId("test-case-tab-system-err")).toBeNull();
  });

  it("should system out tab when test case has system out", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "Test Case",
      packageName: "",
      className: "",
      fullName: "Test Case",
      duration: 1.2,
      passed: true,
      skipped: false,
      hasSystemOut: true,
      hasSystemErr: false,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: true,
      hasSystemErrTestSuite: false,
      failure: null,
      createdTimestamp: moment("2020-04-25").toDate(),
    };

    const { queryByTestId } = render(
      <TestCaseDetails testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-tab-system-out")).not.toBeNull();
    expect(queryByTestId("test-case-tab-system-err")).toBeNull();
  });

  it("should system err tab when test case has system err", () => {
    const testCase: TestCase = {
      idx: 1,
      testSuiteIdx: 2,
      publicId: "12345",
      name: "Test Case",
      packageName: "",
      className: "",
      fullName: "Test Case",
      duration: 1.2,
      passed: true,
      skipped: false,
      hasSystemOut: false,
      hasSystemErr: true,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: false,
      hasSystemErrTestSuite: true,
      failure: null,
      createdTimestamp: moment("2020-04-25").toDate(),
    };

    const { queryByTestId } = render(
      <TestCaseDetails testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-tab-system-err")).not.toBeNull();
    expect(queryByTestId("test-case-tab-system-out")).toBeNull();
  });
});
