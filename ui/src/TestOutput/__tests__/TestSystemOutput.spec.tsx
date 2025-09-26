import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render } from "@testing-library/react";
import { TestOutput } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestOutputType from "../../service/TestOutputType";
import TestSystemOutput from "../TestSystemOutput";
import { globalHistory } from "@reach/router";
import { QueryParamProvider } from "use-query-params";

jest.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

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
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`,
      )
      .reply(200, testSuiteOutput);

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <TestSystemOutput
          publicId={publicId}
          testSuiteIdx={testSuiteIdx}
          outputType={outputType}
        />
      </QueryParamProvider>,
    );

    await findByTestId("code-text");
    expect(queryByTestId("loading-section-error")).toBeNull();
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
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemOut`,
      )
      .reply(200, testCaseOutput);

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <TestSystemOutput
          publicId={publicId}
          testSuiteIdx={testSuiteIdx}
          testCaseIdx={testCaseIdx}
          outputType={outputType}
        />
      </QueryParamProvider>,
    );

    expect(await findByTestId("code-text")).toHaveTextContent("My output");
    expect(queryByTestId("loading-section-error")).toBeNull();
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
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/systemErr`,
      )
      .reply(200, testCaseOutput);

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <TestSystemOutput
          publicId={publicId}
          testSuiteIdx={testSuiteIdx}
          testCaseIdx={testCaseIdx}
          outputType={outputType}
        />
      </QueryParamProvider>,
    );

    expect(await findByTestId("code-text")).toHaveTextContent("My system err");
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching output fails", async () => {
    const publicId = "12345";
    const testSuiteIdx = 17;
    const outputType = TestOutputType.SystemOut;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`,
      )
      .reply(404, {});

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <TestSystemOutput
          publicId={publicId}
          testSuiteIdx={testSuiteIdx}
          outputType={outputType}
        />
      </QueryParamProvider>,
    );

    await findByTestId("loading-section-error");
    expect(queryByTestId("code-text")).toBeNull();
  });
});
