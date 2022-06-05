import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { act, render } from "@testing-library/react";
import moment from "moment-timezone";
import {
  RepositoryFlakyTest,
  RepositoryFlakyTests,
} from "../../../model/RepositoryModel";
import { TestCase } from "../../../model/TestRunModel";
import RepositoryFlakyTestsTable from "../RepositoryFlakyTestsTable";

describe("RepositoryFlakyTestsTable", () => {
  beforeEach(() => {
    // Set the default timezone so it uses the same timezone when
    // running locally and when running in CI.
    moment.tz.setDefault("America/Chicago");
  });

  afterEach(() => {
    moment.tz.setDefault();
  });

  it("should format last timestamp in local timezone", () => {
    const flakyTests = {
      tests: [createFlakyTest("2020-09-01", "2020-10-02")],
    } as RepositoryFlakyTests;

    const { getByTestId } = render(
      <RepositoryFlakyTestsTable flakyTests={flakyTests} />
    );

    expect(getByTestId("flaky-test-case-first-failure-1")).toHaveTextContent(
      "Sep 1st 2020, 5:03 am"
    );

    expect(getByTestId("flaky-test-case-latest-failure-1")).toHaveTextContent(
      "Oct 2nd 2020, 6:03 am"
    );
  });

  it("should sort by first failure", () => {
    const flakyTests = {
      tests: [
        createFlakyTest("2020-09-02", "2020-10-02"),
        createFlakyTest("2020-09-03", "2020-10-03"),
        createFlakyTest("2020-09-01", "2020-10-01"),
      ],
    } as RepositoryFlakyTests;

    const { getByText, getAllByTestId } = render(
      <RepositoryFlakyTestsTable flakyTests={flakyTests} />
    );

    act(() => {
      getByText("First failure").click(); // Initial sort is ascending
    });

    const firstFailureAscending = getAllByTestId(
      "flaky-test-case-first-failure",
      {
        exact: false,
      }
    );
    expect(firstFailureAscending.length).toBe(3);
    expect(firstFailureAscending[0]).toHaveTextContent("Sep 1st 2020");
    expect(firstFailureAscending[1]).toHaveTextContent("Sep 2nd 2020");
    expect(firstFailureAscending[2]).toHaveTextContent("Sep 3rd 2020");

    act(() => {
      getByText("First failure").click(); // Second sort is descending
    });
    const firstFailureDescending = getAllByTestId(
      "flaky-test-case-first-failure",
      {
        exact: false,
      }
    );
    expect(firstFailureDescending.length).toBe(3);
    expect(firstFailureDescending[0]).toHaveTextContent("Sep 3rd 2020");
    expect(firstFailureDescending[1]).toHaveTextContent("Sep 2nd 2020");
    expect(firstFailureDescending[2]).toHaveTextContent("Sep 1st 2020");
  });

  it("should sort by latest failure", () => {
    const flakyTests = {
      tests: [
        createFlakyTest("2020-09-02", "2020-10-02"),
        createFlakyTest("2020-09-03", "2020-10-03"),
        createFlakyTest("2020-09-01", "2020-10-01"),
      ],
    } as RepositoryFlakyTests;

    const { getByText, getAllByTestId } = render(
      <RepositoryFlakyTestsTable flakyTests={flakyTests} />
    );

    act(() => {
      getByText("Latest failure").click(); // Initial sort is ascending
    });
    const latestFailureAscending = getAllByTestId(
      "flaky-test-case-latest-failure",
      {
        exact: false,
      }
    );
    expect(latestFailureAscending.length).toBe(3);
    expect(latestFailureAscending[0]).toHaveTextContent("Oct 1st 2020");
    expect(latestFailureAscending[1]).toHaveTextContent("Oct 2nd 2020");
    expect(latestFailureAscending[2]).toHaveTextContent("Oct 3rd 2020");

    act(() => {
      getByText("Latest failure").click(); // Second sort is descending
    });
    const latestFailureDescending = getAllByTestId(
      "flaky-test-case-latest-failure",
      {
        exact: false,
      }
    );
    expect(latestFailureDescending.length).toBe(3);
    expect(latestFailureDescending[0]).toHaveTextContent("Oct 3rd 2020");
    expect(latestFailureDescending[1]).toHaveTextContent("Oct 2nd 2020");
    expect(latestFailureDescending[2]).toHaveTextContent("Oct 1st 2020");
  });

  function createFlakyTest(
    firstTestCaseCreatedDate: string,
    lastTestCaseCreatedDate: string
  ): RepositoryFlakyTest {
    return {
      testCase: {
        idx: 1,
        testSuiteIdx: 2,
        publicId: "12345",
        packageName: "dev.projektor",
        className: "MyClass",
        fullName: "dev.projektor.MyClass",
        duration: 5.0,
        passed: false,
        skipped: false,
        failure: null,
        hasSystemOut: false,
        hasSystemErr: false,
        createdTimestamp: moment("2020-04-25").toDate(),
      } as TestCase,
      failureCount: 4,
      failurePercentage: 50.0,
      firstTestCase: {
        idx: 1,
        testSuiteIdx: 2,
        publicId: "12346",
        packageName: "dev.projektor",
        className: "MyClass",
        fullName: "dev.projektor.MyClass",
        duration: 5.0,
        passed: false,
        skipped: false,
        failure: null,
        hasSystemOut: false,
        hasSystemErr: false,
        createdTimestamp: moment
          .utc(`${firstTestCaseCreatedDate}T10:03:04.580Z`)
          .toDate(),
      } as TestCase,
      latestTestCase: {
        idx: 1,
        testSuiteIdx: 2,
        publicId: "12347",
        packageName: "dev.projektor",
        className: "MyClass",
        fullName: "dev.projektor.MyClass",
        duration: 5.0,
        passed: false,
        skipped: false,
        failure: null,
        hasSystemOut: false,
        hasSystemErr: false,
        createdTimestamp: moment
          .utc(`${lastTestCaseCreatedDate}T11:03:04.580Z`)
          .toDate(),
      } as TestCase,
    } as RepositoryFlakyTest;
  }
});
