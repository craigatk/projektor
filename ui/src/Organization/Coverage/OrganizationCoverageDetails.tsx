import * as React from "react";
import { OrganizationCoverage } from "../../model/OrganizationModel";
import CoverageGroups from "../../Coverage/CoverageGroups";
import { CoverageGroup } from "../../model/TestRunModel";

interface OrganizationCoverageDetailsProps {
  organizationCoverage: OrganizationCoverage;
}

const OrganizationCoverageDetails = ({
  organizationCoverage,
}: OrganizationCoverageDetailsProps) => {
  const coverageGroups = organizationCoverage.repositories.map(
    (repositoryCoverage) =>
      ({
        name: repositoryCoverage.repoName,
        stats: repositoryCoverage.coverage.overallStats,
      } as CoverageGroup)
  );

  return (
    <div>
      <CoverageGroups
        coverageGroups={coverageGroups}
        pageTitle="Repositories"
        groupHeader="Repository"
      />
    </div>
  );
};

export default OrganizationCoverageDetails;
