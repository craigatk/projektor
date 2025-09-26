import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { TestRun, TestSuite } from "../../model/TestRunModel";
import TestRunAllTests from "../TestRunAllTests";
import { axiosInstance } from "../../service/AxiosService";

jest.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("TestRunAllTests", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render all tests when fetch completes successfully", async () => {
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

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}`)
      .reply(200, testRun);

    const { getByTestId, queryByTestId } = render(
      <TestRunAllTests publicId={publicId} />,
    );

    await waitFor(() => getByTestId("test-suite-list"));

    expect(queryByTestId("test-suite-list")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching all tests fails", async () => {
    const publicId = "failing-id";

    mockAxios.onGet(`http://localhost:8080/run/${publicId}`).reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <TestRunAllTests publicId={publicId} />,
    );

    await waitFor(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("test-suite-list")).toBeNull();
  });
});
