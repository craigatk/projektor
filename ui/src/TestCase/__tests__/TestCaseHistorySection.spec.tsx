import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { axiosInstance } from "../../service/AxiosService";
import {
  TestCaseHistory,
  TestCaseHistoryEntry,
} from "../../model/TestRunModel";
import TestCaseHistorySection from "../TestCaseHistorySection";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

const historyUrl = (publicId: string) =>
  `http://localhost:8080/run/${publicId}/suite/1/case/2/history`;

const entry = (
  publicId: string,
  passed: boolean,
  commitSha: string,
): TestCaseHistoryEntry => ({
  publicId,
  testSuiteIdx: 1,
  testCaseIdx: 2,
  createdTimestamp: new Date("2026-09-01T10:00:00"),
  passed,
  skipped: false,
  failed: !passed,
  duration: 1.5,
  branchName: "main",
  commitSha,
});

describe("TestCaseHistorySection", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should show where the current failure streak started", async () => {
    const failing2 = entry("FAIL2", false, "cccccccccc");
    const failing1 = entry("FAIL1", false, "bbbbbbbbbb");
    const passing = entry("PASS1", true, "aaaaaaaaaa");

    const history: TestCaseHistory = {
      entries: [failing2, failing1, passing],
      firstFailure: failing1,
      lastPassedBeforeFailure: passing,
    };

    mockAxios.onGet(historyUrl("FAIL2")).reply(200, history);

    const { getByTestId } = render(
      <TestCaseHistorySection
        publicId="FAIL2"
        testSuiteIdx={1}
        testCaseIdx={2}
      />,
    );

    await waitFor(() => getByTestId("test-case-history-section"));

    expect(getByTestId("test-case-history-failure-streak")).toHaveTextContent(
      "bbbbbbb",
    );
    expect(getByTestId("test-case-history-failure-streak")).toHaveTextContent(
      "Last passed",
    );
    expect(
      getByTestId("test-case-history-first-failure-link").getAttribute("href"),
    ).toEqual("/tests/FAIL1/suite/1/case/2");
    expect(
      getByTestId("test-case-history-last-passed-link").getAttribute("href"),
    ).toEqual("/tests/PASS1/suite/1/case/2");

    expect(getByTestId("test-case-history-strip").children).toHaveLength(3);
    expect(getByTestId("test-case-history-row-1-result")).toHaveTextContent(
      "failed",
    );
    expect(getByTestId("test-case-history-row-3-result")).toHaveTextContent(
      "passed",
    );
  });

  it("should not show failure streak when test passed", async () => {
    const passing = entry("PASS2", true, "aaaaaaaaaa");

    mockAxios.onGet(historyUrl("PASS2")).reply(200, {
      entries: [passing],
    } as TestCaseHistory);

    const { getByTestId, queryByTestId } = render(
      <TestCaseHistorySection
        publicId="PASS2"
        testSuiteIdx={1}
        testCaseIdx={2}
      />,
    );

    await waitFor(() => getByTestId("test-case-history-section"));

    expect(queryByTestId("test-case-history-failure-streak")).toBeNull();
  });

  it("should show message when no history is available", async () => {
    mockAxios.onGet(historyUrl("NOGIT")).reply(200, {
      entries: [],
    } as TestCaseHistory);

    const { getByTestId } = render(
      <TestCaseHistorySection
        publicId="NOGIT"
        testSuiteIdx={1}
        testCaseIdx={2}
      />,
    );

    await waitFor(() => getByTestId("test-case-history-empty"));
  });
});
