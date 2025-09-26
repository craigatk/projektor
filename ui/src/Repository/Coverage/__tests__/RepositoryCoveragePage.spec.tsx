import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import {
  RepositoryCoverageTimeline,
  RepositoryCoverageTimelineEntry,
} from "../../../model/RepositoryModel";
import moment from "moment";
import { createCoverageStats } from "../../../testUtils/coverageTestUtils";
import RepositoryCoveragePage from "../RepositoryCoveragePage";
import {
  createHistory,
  createMemorySource,
  globalHistory,
  LocationProvider,
} from "@reach/router";
import ResizeObserver from "resize-observer-polyfill";
import { QueryParamProvider } from "use-query-params";
import { CoverageExists } from "../../../model/TestRunModel";

window.ResizeObserver = ResizeObserver;

jest.mock("../../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("RepositoryCoveragePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should display coverage timeline graph from mainline when no project name", async () => {
    const repoName = "my-org/my-repo";

    const timeline = {
      timelineEntries: [
        {
          publicId: "COV1",
          createdTimestamp: moment("2020-09-10").toDate(),
          coverageStats: createCoverageStats(89.01),
        } as RepositoryCoverageTimelineEntry,
        {
          publicId: "COV2",
          createdTimestamp: moment("2020-09-11").toDate(),
          coverageStats: createCoverageStats(90.01),
        } as RepositoryCoverageTimelineEntry,
        {
          publicId: "COV3",
          createdTimestamp: moment("2020-09-12").toDate(),
          coverageStats: createCoverageStats(91.01),
        } as RepositoryCoverageTimelineEntry,
      ],
    } as RepositoryCoverageTimeline;

    const coverageExists = {
      exists: true,
    } as CoverageExists;

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/exists`)
      .reply(200, coverageExists);

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/timeline`)
      .reply(200, timeline);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider reachHistory={globalHistory}>
          <RepositoryCoveragePage orgPart="my-org" repoPart="my-repo" />
        </QueryParamProvider>
      </LocationProvider>,
    );

    await findByTestId("repository-coverage-timeline-graph");
  });

  it("should not display coverage section when coverage does not exist for given repo", async () => {
    const repoName = "my-org/my-no-coverage-repo";

    const coverageExists = {
      exists: false,
    } as CoverageExists;

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/exists`)
      .reply(200, coverageExists);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider reachHistory={globalHistory}>
          <RepositoryCoveragePage
            orgPart="my-org"
            repoPart="my-no-coverage-repo"
          />
        </QueryParamProvider>
      </LocationProvider>,
    );

    await findByTestId("repo-page-no-coverage");
  });

  it("should display message when no coverage timeline for given branch", async () => {
    const repoName = "my-org/my-no-coverage-branch";

    const coverageExists = {
      exists: true,
    } as CoverageExists;

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/exists`)
      .reply(200, coverageExists);

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/timeline`)
      .reply(200);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider reachHistory={globalHistory}>
          <RepositoryCoveragePage
            orgPart="my-org"
            repoPart="my-no-coverage-branch"
          />
        </QueryParamProvider>
      </LocationProvider>,
    );

    await findByTestId("repo-results-no-coverage");
  });
});
