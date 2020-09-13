import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import OrganizationCoveragePage from "../OrganizationCoveragePage";
import {
  OrganizationCoverage,
  RepositoryCoverage,
} from "../../../model/OrganizationModel";
import {
  Coverage,
  CoverageStat,
  CoverageStats,
} from "../../../model/TestRunModel";

describe("OrganizationCoveragePage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("when three repos in org should display their coverage", async () => {
    const orgName = "my-org";

    const repoCoverage1 = {
      publicId: "repoCov1",
      repoName: `${orgName}/repo1`,
      coverage: {
        groups: [],
        overallStats: {
          lineStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 90.11,
          } as CoverageStat,
          statementStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 91.11,
          } as CoverageStat,
          branchStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 92.11,
          } as CoverageStat,
        } as CoverageStats,
      } as Coverage,
    } as RepositoryCoverage;

    const repoCoverage2 = {
      publicId: "repoCov2",
      repoName: `${orgName}/repo2`,
      coverage: {
        groups: [],
        overallStats: {
          lineStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 93.11,
          } as CoverageStat,
          statementStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 94.11,
          } as CoverageStat,
          branchStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 95.11,
          } as CoverageStat,
        } as CoverageStats,
      } as Coverage,
    } as RepositoryCoverage;

    const repoCoverage3 = {
      publicId: "repoCov3",
      repoName: `${orgName}/repo3`,
      coverage: {
        groups: [],
        overallStats: {
          lineStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 96.11,
          } as CoverageStat,
          statementStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 97.11,
          } as CoverageStat,
          branchStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 98.11,
          } as CoverageStat,
        } as CoverageStats,
      } as Coverage,
    } as RepositoryCoverage;

    const organizationCoverage = {
      repositories: [repoCoverage1, repoCoverage2, repoCoverage3],
    } as OrganizationCoverage;

    mockAxios
      .onGet(`http://localhost:8080/org/${orgName}/coverage`)
      .reply(200, organizationCoverage);

    const { findByTestId, getByTestId } = render(
      <OrganizationCoveragePage orgName={orgName} />
    );

    expect(await findByTestId("coverage-name-1")).toHaveTextContent(
      `${orgName}/repo1`
    );
    expect(getByTestId("coverage-name-2")).toHaveTextContent(
      `${orgName}/repo2`
    );
    expect(getByTestId("coverage-name-3")).toHaveTextContent(
      `${orgName}/repo3`
    );

    expect(
      getByTestId("line-coverage-row-1-covered-percentage")
    ).toHaveTextContent("90.11%");
    expect(
      getByTestId("statement-coverage-row-1-covered-percentage")
    ).toHaveTextContent("91.11%");
    expect(
      getByTestId("branch-coverage-row-1-covered-percentage")
    ).toHaveTextContent("92.11%");

    expect(
      getByTestId("line-coverage-row-2-covered-percentage")
    ).toHaveTextContent("93.11%");
    expect(
      getByTestId("statement-coverage-row-2-covered-percentage")
    ).toHaveTextContent("94.11%");
    expect(
      getByTestId("branch-coverage-row-2-covered-percentage")
    ).toHaveTextContent("95.11%");

    expect(
      getByTestId("line-coverage-row-3-covered-percentage")
    ).toHaveTextContent("96.11%");
    expect(
      getByTestId("statement-coverage-row-3-covered-percentage")
    ).toHaveTextContent("97.11%");
    expect(
      getByTestId("branch-coverage-row-3-covered-percentage")
    ).toHaveTextContent("98.11%");
  });

  it("should display project name when it is set", async () => {
    const orgName = "my-proj-org";

    const repoCoverage1 = {
      publicId: "repoCov1",
      repoName: `${orgName}/repo1`,
      projectName: "server",
      coverage: {
        groups: [],
        overallStats: {
          lineStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 90.11,
          } as CoverageStat,
          statementStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 91.11,
          } as CoverageStat,
          branchStat: {
            covered: 10,
            missed: 1,
            total: 11,
            coveredPercentage: 92.11,
          } as CoverageStat,
        } as CoverageStats,
      } as Coverage,
    } as RepositoryCoverage;

    const organizationCoverage = {
      repositories: [repoCoverage1],
    } as OrganizationCoverage;

    mockAxios
      .onGet(`http://localhost:8080/org/${orgName}/coverage`)
      .reply(200, organizationCoverage);

    const { findByTestId } = render(
      <OrganizationCoveragePage orgName={orgName} />
    );

    expect(await findByTestId("coverage-name-1")).toHaveTextContent(
      `${orgName}/repo1 server`
    );
  });

  it("should not display coverage data when the organization doesn't have any repos with coverage", async () => {
    const orgName = "no-coverage";

    mockAxios.onGet(`http://localhost:8080/org/${orgName}/coverage`).reply(204);

    const { getByTestId, findByTestId } = render(
      <OrganizationCoveragePage orgName={orgName} />
    );

    expect(
      await findByTestId("organization-coverage-no-details")
    ).toHaveTextContent(
      `No repositories found with coverage in organization ${orgName}`
    );
  });
});
