import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { TestSuite } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestSuitePage from "../TestSuitePage";

describe("TestSuitePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render test suite when fetch completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    // @ts-ignore
    const testSuite = {
      idx: testSuiteIdx,
      className: "MyClass",
    } as TestSuite;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}`)
      .reply(200, testSuite);

    const { getByTestId, queryByTestId } = render(
      <TestSuitePage publicId={publicId} testSuiteIdx={testSuiteIdx} />,
    );

    await waitFor(() => getByTestId("test-suite-details"));

    expect(queryByTestId("test-suite-details")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching test suite fails", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}`)
      .reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <TestSuitePage publicId={publicId} testSuiteIdx={testSuiteIdx} />,
    );

    await waitFor(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("test-suite-details")).toBeNull();
  });
});
