import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import {
  Attachment,
  AttachmentType,
  TestCase,
  TestFailure,
} from "../../model/TestRunModel";
import TestCaseFailurePanel from "../TestCaseFailurePanel";
import moment from "moment";

describe("TestCaseFailurePanel", () => {
  it("should render failure details link when the test case failed", () => {
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
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-summary-failure-link-2-1")).not.toBeNull();

    expect(queryByTestId("test-case-summary-system-out-link-2-1")).toBeNull();
    expect(queryByTestId("test-case-summary-system-err-link-2-1")).toBeNull();
  });

  it("should not render failure details link when the test case passed", () => {
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
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-summary-failure-link-2-1")).toBeNull();

    expect(queryByTestId("test-case-summary-system-out-link-2-1")).toBeNull();
    expect(queryByTestId("test-case-summary-system-err-link-2-1")).toBeNull();
  });

  it("should render system out link when the test case has system out", () => {
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
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(
      queryByTestId("test-case-summary-system-out-link-2-1")
    ).not.toBeNull();
    expect(queryByTestId("test-case-summary-system-err-link-2-1")).toBeNull();
  });

  it("should render system err link when the test case has system err", () => {
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
      hasSystemErr: true,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: false,
      hasSystemErrTestSuite: true,
      failure: null,
      createdTimestamp: moment("2020-04-25").toDate(),
    };

    const { queryByTestId } = render(
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-summary-system-out-link-2-1")).toBeNull();
    expect(
      queryByTestId("test-case-summary-system-err-link-2-1")
    ).not.toBeNull();
  });

  it("should render both system out and err links when the test case has both", () => {
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
      hasSystemOut: true,
      hasSystemErr: true,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: true,
      hasSystemErrTestSuite: true,
      failure: null,
      createdTimestamp: moment("2020-04-25").toDate(),
    };

    const { queryByTestId } = render(
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(
      queryByTestId("test-case-summary-system-out-link-2-1")
    ).not.toBeNull();
    expect(
      queryByTestId("test-case-summary-system-err-link-2-1")
    ).not.toBeNull();
  });

  it("should render failure message when flag to show full failure is false", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My failure text",
      failureType: "",
    };

    const testCase = createTestCaseWithFailure(failure);

    const { getByTestId } = render(
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(getByTestId("test-case-failure-text-2-1")).toHaveTextContent(
      "My failure message"
    );
  });

  it("should render failure text when flag to show full failure is true", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My failure text",
      failureType: "",
    };

    const testCase = createTestCaseWithFailure(failure);

    const { getByTestId } = render(
      <TestCaseFailurePanel
        testCase={testCase}
        publicId="12345"
        showFullFailure={true}
      />
    );

    expect(getByTestId("test-case-failure-text-2-1")).toHaveTextContent(
      "My failure text"
    );
  });

  it("should show attachment screenshot when one exists", () => {
    const failure: TestFailure = {
      failureMessage: "My failure message",
      failureText: "My failure text",
      failureType: "",
    };

    const attachment: Attachment = {
      fileName: "screenshot.png",
      objectName: "screenshot-object",
      attachmentType: AttachmentType.IMAGE,
    };

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
      failure: failure,
      createdTimestamp: moment("2020-04-25").toDate(),
      attachments: [attachment],
    };

    const { queryByTestId } = render(
      <TestCaseFailurePanel testCase={testCase} publicId="12345" />
    );

    expect(queryByTestId("test-case-failure-screenshot-2-1")).not.toBeNull();
  });

  function createTestCaseWithFailure(failure: TestFailure): TestCase {
    return {
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
      hasSystemOut: true,
      hasSystemErr: true,
      hasSystemOutTestCase: false,
      hasSystemErrTestCase: false,
      hasSystemOutTestSuite: true,
      hasSystemErrTestSuite: true,
      createdTimestamp: moment("2020-04-25").toDate(),
      failure,
    };
  }
});
