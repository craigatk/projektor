import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { TestOutput } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestOutputType from "../../service/TestOutputType";
import TestSystemOutput from "../TestSystemOutput";
import { act } from "react-dom/test-utils";
import { globalHistory } from "@reach/router";
import { QueryParamProvider } from "use-query-params";

describe("TestSystemOut", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render output when fetching test suite output completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const outputType = TestOutputType.SystemOut;

    const testSuiteOutput = {
      value: "My output",
    } as TestOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`
      )
      .reply(200, testSuiteOutput);

    await act(async () => {
      const { getByTestId, queryByTestId } = render(
        <QueryParamProvider reachHistory={globalHistory}>
          <TestSystemOutput
            publicId={publicId}
            testSuiteIdx={testSuiteIdx}
            outputType={outputType}
          />
        </QueryParamProvider>
      );

      await waitFor(() => getByTestId("code-text"));

      expect(queryByTestId("code-text")).not.toBeNull();
      expect(queryByTestId("loading-section-error")).toBeNull();
    });
  });

  it("should render output when fetching test case system out completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;
    const outputType = TestOutputType.SystemOut;

    const testCaseOutput = {
      value: "My output",
    } as TestOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemOut`
      )
      .reply(200, testCaseOutput);

    await act(async () => {
      const { getByTestId, queryByTestId } = render(
        <QueryParamProvider reachHistory={globalHistory}>
          <TestSystemOutput
            publicId={publicId}
            testSuiteIdx={testSuiteIdx}
            testCaseIdx={testCaseIdx}
            outputType={outputType}
          />
        </QueryParamProvider>
      );

      await waitFor(() => getByTestId("code-text"));

      expect(queryByTestId("code-text")).toHaveTextContent("My output");
      expect(queryByTestId("loading-section-error")).toBeNull();
    });
  });

  it("should render output when fetching test case system err completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;
    const outputType = TestOutputType.SystemErr;

    const testCaseOutput = {
      value: "My system err",
    } as TestOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemErr`
      )
      .reply(200, testCaseOutput);

    await act(async () => {
      const { getByTestId, queryByTestId } = render(
        <QueryParamProvider reachHistory={globalHistory}>
          <TestSystemOutput
            publicId={publicId}
            testSuiteIdx={testSuiteIdx}
            testCaseIdx={testCaseIdx}
            outputType={outputType}
          />
        </QueryParamProvider>
      );

      await waitFor(() => getByTestId("code-text"));

      expect(queryByTestId("code-text")).toHaveTextContent("My system err");
      expect(queryByTestId("loading-section-error")).toBeNull();
    });
  });

  it("should render error when fetching output fails", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const outputType = TestOutputType.SystemOut;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`
      )
      .reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <TestSystemOutput
          publicId={publicId}
          testSuiteIdx={testSuiteIdx}
          outputType={outputType}
        />
      </QueryParamProvider>
    );

    await waitFor(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("code-text")).toBeNull();
  });
});
