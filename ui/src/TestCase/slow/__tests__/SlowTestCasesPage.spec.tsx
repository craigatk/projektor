import "@testing-library/jest-dom";
import React from "react";
import { render, waitFor } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import { SlowTestRegressions, TestCase } from "../../../model/TestRunModel";
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

  const testCase = {
    idx: 1,
    testSuiteIdx: 1,
    name: "should do the thing slowly",
    duration: 42.5,
  } as TestCase;

  it("should fetch and render the slowest test cases", async () => {
    const publicId = "TESTRUN1";

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow`)
      .reply(200, [testCase]);

    const { findByTestId } = render(<SlowTestCasesPage publicId={publicId} />);

    expect(await findByTestId("slow-test-cases-title")).toHaveTextContent(
      "Slowest test cases",
    );
    expect(await findByTestId("test-case-name-1-1")).toHaveTextContent(
      "should do the thing slowly",
    );
  });

  it("should show regressions and the slowest tests in separate sections", async () => {
    const publicId = "TESTRUN2";

    const slowTestRegressions: SlowTestRegressions = {
      thresholdPercent: 50,
      baselineRunCount: 5,
      regressions: [
        {
          testCase,
          baselineDuration: 20.0,
          baselineSampleCount: 5,
          durationIncrease: 22.5,
          increasePercent: 112.5,
        },
      ],
    };

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow`)
      .reply(200, [testCase]);
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow/regressions`)
      .reply(200, slowTestRegressions);

    const { findByTestId } = render(<SlowTestCasesPage publicId={publicId} />);

    expect(await findByTestId("slow-test-regressions-title")).toHaveTextContent(
      "Slower than baseline",
    );
    expect(
      await findByTestId("slowest-test-cases-section-title"),
    ).toHaveTextContent("Slowest in this run");
  });

  it("should not show section headers when there are no regressions", async () => {
    const publicId = "TESTRUN3";

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow`)
      .reply(200, [testCase]);
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow/regressions`)
      .reply(200, {
        thresholdPercent: 50,
        baselineRunCount: 0,
        regressions: [],
      });

    const { findByTestId, queryByTestId } = render(
      <SlowTestCasesPage publicId={publicId} />,
    );

    await findByTestId("test-case-name-1-1");
    await waitFor(() =>
      expect(
        mockAxios.history.get.filter((request) =>
          request.url.endsWith("/regressions"),
        ),
      ).toHaveLength(1),
    );
    expect(queryByTestId("slow-test-regressions")).not.toBeInTheDocument();
    expect(
      queryByTestId("slowest-test-cases-section-title"),
    ).not.toBeInTheDocument();
  });
});
