import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import OrganizationCoverageDetails from "../OrganizationCoverageDetails";
import {
  OrganizationCoverage,
  RepositoryCoverage,
} from "../../../model/OrganizationModel";
import {
  Coverage,
  CoverageStat,
  CoverageStats,
} from "../../../model/TestRunModel";

describe("OrganizationCoverageDetails", () => {
  it("should render coverage details even when one project doesn't have any coverage stats", async () => {
    const orgName = "my-org";
    const organizationCoverage = {
      repositories: [
        {
          publicId: "no-coverage-id",
          repoName: "no-coverage-repo",
          coverage: null,
        } as RepositoryCoverage,
        {
          publicId: "has-coverage-id",
          repoName: "has-coverage-repo",
          coverage: {
            groups: [],
            overallStats: {
              branchStat: {
                covered: 80,
                missed: 20,
                total: 100,
                coveredPercentage: 80,
              } as CoverageStat,
              lineStat: {
                covered: 80,
                missed: 20,
                total: 100,
                coveredPercentage: 80,
              } as CoverageStat,
              statementStat: {
                covered: 80,
                missed: 20,
                total: 100,
                coveredPercentage: 80,
              } as CoverageStat,
            } as CoverageStats,
          } as Coverage,
        } as RepositoryCoverage,
      ],
    } as OrganizationCoverage;

    const { findByTestId } = render(
      <OrganizationCoverageDetails
        orgName={orgName}
        organizationCoverage={organizationCoverage}
      />,
    );

    expect(await findByTestId("coverage-name-1")).toHaveTextContent(
      `has-coverage-repo`,
    );
  });
});
