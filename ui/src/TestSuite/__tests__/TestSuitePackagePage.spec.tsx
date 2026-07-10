import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../service/AxiosService";
import { TestSuite } from "../../model/TestRunModel";
import TestSuitePackagePage from "../TestSuitePackagePage";
import {
  createHistory,
  createMemorySource,
  LocationProvider,
  globalHistory,
} from "@reach/router";
import { QueryParamProvider } from "use-query-params";
import { ReachAdapter } from "use-query-params/adapters/reach";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("TestSuitePackagePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render test suites in the given package", async () => {
    const publicId = "TESTRUN1";
    const packageName = "com.example";

    const testSuites = [
      {
        idx: 1,
        packageName,
        className: "MyTestClass",
      } as TestSuite,
    ];

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suites?package=${packageName}`,
      )
      .reply(200, testSuites);

    // use-query-params' ReachAdapter reads/writes @reach/router's global
    // history singleton rather than the LocationProvider below (which only
    // needs to exist so <Link>-based components have router context).
    globalHistory.navigate(`?name=${packageName}`);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider adapter={ReachAdapter}>
          <TestSuitePackagePage publicId={publicId} />
        </QueryParamProvider>
      </LocationProvider>,
    );

    expect(
      await findByTestId("test-suite-package-details"),
    ).toBeInTheDocument();
    expect(
      await findByTestId("test-suite-package-name-header"),
    ).toHaveTextContent(`Tests in package ${packageName}`);
    expect(await findByTestId("test-suite-class-name-1")).toHaveTextContent(
      "MyTestClass",
    );
  });

  it("should render an error message when the fetch fails", async () => {
    const publicId = "TESTRUN2";
    const packageName = "com.example";

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/suites?package=${packageName}`,
      )
      .reply(500);

    globalHistory.navigate(`?name=${packageName}`);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider adapter={ReachAdapter}>
          <TestSuitePackagePage publicId={publicId} />
        </QueryParamProvider>
      </LocationProvider>,
    );

    expect(await findByTestId("loading-section-error")).toBeInTheDocument();
  });
});
