import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor, getNodeText } from "@testing-library/react";
import { TestSuiteOutput } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestSuiteSystemOut from "../TestSuiteSystemOut";

describe("TestSuiteSystemOut", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render system out", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;

    const systemOut = {
      value: "My system out",
    } as TestSuiteOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemOut`
      )
      .reply(200, systemOut);

    const { findByTestId, queryByTestId } = render(
      <TestSuiteSystemOut publicId={publicId} testSuiteIdx={testSuiteIdx} />
    );

    await findByTestId("code-text");

    expect(
      getNodeText(await findByTestId("code-text-line-content-1"))
    ).toContain("My system out");

    expect(queryByTestId("loading-section-error")).toBeNull();
  });
});
