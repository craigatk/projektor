import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { render, waitFor } from "@testing-library/react";
import { axiosInstance } from "../../service/AxiosService";
import { TestCaseDebugContext } from "../../model/TestRunModel";
import TestCaseDebugContextSection from "../TestCaseDebugContextSection";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("TestCaseDebugContextSection", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should load and display debug context markdown with copy button", async () => {
    const publicId = "12345";
    const testSuiteIdx = 1;
    const testCaseIdx = 2;

    const debugContext = {
      markdown: "# Test Failure: some test\n\n**Message:**\nassertion failed",
    } as TestCaseDebugContext;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/debug-context`,
      )
      .reply(200, debugContext);

    const { getByTestId } = render(
      <TestCaseDebugContextSection
        publicId={publicId}
        testSuiteIdx={testSuiteIdx}
        testCaseIdx={testCaseIdx}
      />,
    );

    await waitFor(() => getByTestId("test-case-debug-context-text"));

    expect(getByTestId("test-case-debug-context-text")).toHaveTextContent(
      "Test Failure: some test",
    );

    expect(getByTestId("test-case-debug-context-copy-button")).toBeTruthy();
  });
});
