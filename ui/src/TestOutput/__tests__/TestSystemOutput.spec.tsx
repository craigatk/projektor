import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { TestSuiteOutput } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestSuiteOutputType from "../../service/TestSuiteOutputType";
import TestSystemOutput from "../TestSystemOutput";

describe("TestSystemOut", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should render output when fetch completes successfully", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const outputType = TestSuiteOutputType.SystemOut;

    const testSuiteOutput = {
      value: "My output",
    } as TestSuiteOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`
      )
      .reply(200, testSuiteOutput);

    const { getByTestId, queryByTestId } = render(
      <TestSystemOutput
        publicId={publicId}
        testSuiteIdx={testSuiteIdx}
        outputType={outputType}
      />
    );

    await waitFor(() => getByTestId("code-text"));

    expect(queryByTestId("code-text")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should render error when fetching output fails", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const outputType = TestSuiteOutputType.SystemOut;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`
      )
      .reply(404, {});

    const { getByTestId, queryByTestId } = render(
      <TestSystemOutput
        publicId={publicId}
        testSuiteIdx={testSuiteIdx}
        outputType={outputType}
      />
    );

    await waitFor(() => getByTestId("loading-section-error"));

    expect(queryByTestId("loading-section-error")).not.toBeNull();
    expect(queryByTestId("code-text")).toBeNull();
  });
});
