import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import { TestCase } from "../../../model/TestRunModel";
import SlowTestCasesPage from "../SlowTestCasesPage";

vi.mock("../../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("SlowTestCasesPage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render the slowest test cases", async () => {
    const publicId = "TESTRUN1";

    const testCases = [
      {
        idx: 1,
        testSuiteIdx: 1,
        name: "should do the thing slowly",
        duration: 42.5,
      } as TestCase,
    ];

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow`)
      .reply(200, testCases);

    const { findByTestId } = render(<SlowTestCasesPage publicId={publicId} />);

    expect(await findByTestId("slow-test-cases-title")).toHaveTextContent(
      "Slowest test cases",
    );
    expect(await findByTestId("test-case-name-1-1")).toHaveTextContent(
      "should do the thing slowly",
    );
  });
});
