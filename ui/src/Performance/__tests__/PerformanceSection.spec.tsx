import "@testing-library/jest-dom";
import * as React from "react";
import MockAdapter from "axios-mock-adapter";
import { render } from "@testing-library/react";
import { axiosInstance } from "../../service/AxiosService";
import {
  PerformanceResult,
  PerformanceResults,
} from "../../model/TestRunModel";
import PerformanceSection from "../PerformanceSection";

describe("PerformanceSection", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch performance results and render them", async () => {
    const publicId = "12345";

    const performanceResult = {
      name: "perf-1",
      requestsPerSecond: 450.123,
      requestCount: 5000,
      average: 23.345,
      maximum: 59.293,
      p95: 41.293,
    } as PerformanceResult;

    const performanceResults = {
      results: [performanceResult],
    } as PerformanceResults;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/performance`)
      .reply(200, performanceResults);

    const { findByTestId, getByTestId } = render(
      <PerformanceSection publicId={publicId} />,
    );

    await findByTestId("performance-results-table");

    expect(getByTestId("performance-result-name-1")).toHaveTextContent(
      "perf-1",
    );
    expect(
      getByTestId("performance-result-requests-per-second-1"),
    ).toHaveTextContent("450.123");
    expect(getByTestId("performance-result-request-count-1")).toHaveTextContent(
      "5000",
    );
    expect(getByTestId("performance-result-average-1")).toHaveTextContent(
      "23.345 ms",
    );
    expect(getByTestId("performance-result-maximum-1")).toHaveTextContent(
      "59.293 ms",
    );
    expect(getByTestId("performance-result-p95-1")).toHaveTextContent(
      "41.293 ms",
    );
  });
});
