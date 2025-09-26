import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { Attachments, TestRunSummary } from "../../model/TestRunModel";
import {
  axiosInstance,
  axiosInstanceWithoutCache,
} from "../../service/AxiosService";
import TestRunDataWrapper from "../TestRunDataWrapper";
jest.mock("../../Attachments/byteFormat", () => ({
  formatBytes: jest.fn(),
}));
jest.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("TestRunDataWrapper", () => {
  let mockAxios;
  let mockAxiosWithoutCache;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);

    // @ts-ignore
    mockAxiosWithoutCache = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
    mockAxiosWithoutCache.restore();
  });

  it("should render wrapper when fetch completes successfully", async () => {
    const publicId = "12345";
    const testRunSummary = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: false,
      cumulativeDuration: 10.0,
      averageDuration: 2.5,
      slowestTestCaseDuration: 5.0,
    } as TestRunSummary;

    const attachments = {
      attachments: [
        {
          fileName: "attachment1.txt",
        },
      ],
    } as Attachments;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/summary`)
      .reply(200, testRunSummary);

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/failed`)
      .reply(200, []);

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/attachments`)
      .reply(200, attachments);

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/performance`)
      .reply(204);

    const { getByTestId, queryByTestId } = render(
      <TestRunDataWrapper publicId={publicId} />,
    );

    await waitFor(() => getByTestId("test-run-menu-wrapper"));

    expect(queryByTestId("test-run-menu-wrapper")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render processing message when fetching test summary fails and results are still processing", async () => {
    const publicId = "failing-test-summary-id";

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/summary`)
      .reply(404, {});

    mockAxiosWithoutCache
      .onGet(`http://localhost:8080/results/${publicId}/status`)
      .reply(200, { status: "PROCESSING" });

    const { getByTestId, queryByTestId } = render(
      <TestRunDataWrapper publicId={publicId} />,
    );

    await waitFor(() => getByTestId("results-still-processing"));

    expect(queryByTestId("results-still-processing")).not.toBeNull();
    expect(queryByTestId("test-run-menu-wrapper")).toBeNull();
  });
});
