import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
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
      tests: [
        {
          testCase: {
            idx: 1,
            testSuiteIdx: 2,
            packageName: "dev.projektor",
            className: "MyClass",
            fullName: "dev.projektor.MyClass",
            duration: 5.0,
            passed: false,
            skipped: false,
            failure: null,
            hasSystemOut: false,
            hasSystemErr: false,
          } as TestCase,
          failureCount: 4,
          failurePercentage: 50.0,
          latestCreatedTimestamp: moment
            .utc("2020-10-02T11:03:04.580Z")
            .toDate(),
          latestPublicId: "PUB12345",
        } as RepositoryFlakyTest,
      ],
    } as RepositoryFlakyTests;

    const { getByTestId } = render(
      <RepositoryFlakyTestsTable flakyTests={flakyTests} />
    );

    expect(getByTestId("flaky-test-case-latest-failure-1")).toHaveTextContent(
      "Oct 2nd 2020, 6:03 am"
    );
  });
});
