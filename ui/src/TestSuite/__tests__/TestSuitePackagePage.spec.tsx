import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { TestSuite } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestSuitePackagePage from "../TestSuitePackagePage";

describe("TestSuitePackagePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render test suites when fetch completes successfully", async () => {
    const publicId = "12345";
    const packageName = "com.mypackage";
    // @ts-ignore
    const testSuite = {
      idx: 1,
      className: "MyClass",
    } as TestSuite;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suites?package=${packageName}`
      )
      .reply(200, [testSuite]);

    const { getByTestId, queryByTestId } = render(
      <TestSuitePackagePage publicId={publicId} packageName={packageName} />
    );

    await waitFor(() => getByTestId("test-suite-package-details"));

    expect(queryByTestId("test-suite-package-details")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching test suites fails", async () => {
    const publicId = "12345";
    const packageName = "com.mypackage";

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suites?package=${packageName}`
      )
      .reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <TestSuitePackagePage publicId={publicId} packageName={packageName} />
    );

    await waitFor(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("test-suite-package-details")).toBeNull();
  });
});
