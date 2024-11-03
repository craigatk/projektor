import * as React from "react";
import classes from "./OrganizationCoverageDetails.module.css";
import { OrganizationCoverage } from "../../model/OrganizationModel";
import CoverageTable from "../../Coverage/CoverageTable";
import CoverageTableRow from "../../Coverage/CoverageTableRow";
import { Typography } from "@mui/material";
import { repositoryLinkUrlUI } from "../../Repository/RepositoryLink";

interface OrganizationCoverageDetailsProps {
  orgName: string;
  organizationCoverage: OrganizationCoverage;
}

const OrganizationCoverageDetails = ({
  orgName,
  organizationCoverage,
}: OrganizationCoverageDetailsProps) => {
  if (organizationCoverage) {
    const coverageTableRows = organizationCoverage.repositories
      .filter((repositoryCoverage) => repositoryCoverage.coverage != null)
      .map(
        (repositoryCoverage) =>
          ({
            name: repositoryCoverage.projectName
              ? `${repositoryCoverage.repoName} ${repositoryCoverage.projectName}`
              : repositoryCoverage.repoName,
            stats: repositoryCoverage.coverage.overallStats,
            previousTestRunId: repositoryCoverage.coverage.previousTestRunId,
            nameLinkUrl: repositoryLinkUrlUI(
              repositoryCoverage.repoName,
              repositoryCoverage.projectName,
              null,
            ),
            coveredPercentageLink: `/tests/${repositoryCoverage.publicId}/`,
          }) as CoverageTableRow,
      );

    return (
      <div data-testid="organization-coverage-details">
        <CoverageTable
          rows={coverageTableRows}
          pageTitle="Repositories"
          groupHeader="Repository"
        />
      </div>
    );
  } else {
    return (
      <div
        data-testid="organization-coverage-no-details"
        className={classes.noCoverage}
      >
        <Typography align="center">
          No repositories found with coverage in organization {orgName}
        </Typography>
      </div>
    );
  }
};

export default OrganizationCoverageDetails;
