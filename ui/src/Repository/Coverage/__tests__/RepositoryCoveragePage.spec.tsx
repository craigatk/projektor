import "@testing-library/jest-dom/extend-expect";
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

window.ResizeObserver = ResizeObserver;

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

  it("should display message when no coverage timeline", async () => {
    const repoName = "my-org/my-repo";

    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/coverage/timeline`)
      .reply(200);

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <QueryParamProvider reachHistory={globalHistory}>
          <RepositoryCoveragePage orgPart="my-org" repoPart="my-repo" />
        </QueryParamProvider>
      </LocationProvider>,
    );

    await findByTestId("repo-no-coverage");
  });
});
