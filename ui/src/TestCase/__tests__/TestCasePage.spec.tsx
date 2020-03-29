import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, wait } from "@testing-library/react";
import { TestCase, TestSuite } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestCasePage from "../TestCasePage";

describe("TestCasePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render test case when fetch completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    const testCase = {
      idx: testCaseIdx,
      testSuiteIdx,
      packageName: null,
      className: "MyClass",
      duration: 5.0,
      passed: false,
      skipped: false,
      failure: null,
      hasSystemOut: false,
      hasSystemErr: false
    } as TestCase;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
      )
      .reply(200, testCase);

    const { getByTestId, queryByTestId } = render(
      <TestCasePage
        publicId={publicId}
        testSuiteIdx={testSuiteIdx}
        testCaseIdx={testCaseIdx}
      />
    );

    await wait(() => getByTestId("test-case-details"));

    expect(queryByTestId("test-case-details")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching test case fails", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
      )
      .reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <TestCasePage
        publicId={publicId}
        testSuiteIdx={testSuiteIdx}
        testCaseIdx={testCaseIdx}
      />
    );

    await wait(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("test-case-details")).toBeNull();
  });
});
