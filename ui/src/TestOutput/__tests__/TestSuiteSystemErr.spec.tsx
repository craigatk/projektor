import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, wait, getNodeText } from "@testing-library/react";
import { TestSuiteOutput } from "../../model/TestRunModel";
import { axiosInstance } from "../../service/AxiosService";
import TestSuiteSystemErr from "../TestSuiteSystemErr";

describe("TestSuiteSystemErr", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render system err", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;

    const systemErr = {
      value: "My system err"
    } as TestSuiteOutput;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/systemErr`
      )
      .reply(200, systemErr);

    const { getByTestId, queryByTestId } = render(
      <TestSuiteSystemErr publicId={publicId} testSuiteIdx={testSuiteIdx} />
    );

    await wait(() => getByTestId("code-text"));

    expect(queryByTestId("code-text")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();

    expect(getNodeText(getByTestId("code-text-line-content-1"))).toContain(
      "My system err"
    );
  });
});
