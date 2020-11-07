import "@testing-library/jest-dom/extend-expect";
import * as React from "react";
import { render, waitFor } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../service/AxiosService";
import { TestRun, TestRunSummary, TestSuite } from "../../model/TestRunModel";
import Dashboard from "../Dashboard";
import { PinState } from "../../Pin/PinState";

describe("Dashboard", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  const publicId = "12345";
  // @ts-ignore
  const testSuite = {
    idx: 1,
    className: "MyClass",
  } as TestSuite;

  // @ts-ignore
  const testRun = {
    id: publicId,
    testSuites: [testSuite],
  } as TestRun;

  it("should show all tests when they passed and there is at least one test", async () => {
    const testRunSummary = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: true,
      cumulativeDuration: 10.0,
      averageDuration: 2.5,
      slowestTestCaseDuration: 5.0,
    } as TestRunSummary;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}`)
      .reply(200, testRun);

    const { getByTestId } = render(
      <PinState publicId={publicId}>
        <Dashboard publicId={publicId} testRunSummary={testRunSummary} />
      </PinState>
    );

    await waitFor(() => getByTestId("test-suite-list"));
  });

  it("should not show all tests when they passed and there are no tests", async () => {
    const testRunSummary = {
      id: publicId,
      totalTestCount: 0,
      totalPassingCount: 0,
      totalSkippedCount: 0,
      totalFailureCount: 0,
      passed: true,
      cumulativeDuration: 0,
      averageDuration: 0,
      slowestTestCaseDuration: 0,
    } as TestRunSummary;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}`)
      .reply(200, testRun);

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <Dashboard publicId={publicId} testRunSummary={testRunSummary} />
      </PinState>
    );

    expect(queryByTestId("test-suite-list")).toBeNull();
  });
});
