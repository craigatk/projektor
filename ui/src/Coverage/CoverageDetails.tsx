import * as React from "react";
import classes from "./CoverageDetails.module.css";
import { Coverage } from "../model/TestRunModel";
import { Typography } from "@material-ui/core";
import OverallCoverageGraphs from "./OverallCoverageGraphs";
import CoverageTable from "./CoverageTable";
import CoverageTableRow from "./CoverageTableRow";

interface CoverageDetailsProps {
  coverage: Coverage;
  publicId: string;
}

const CoverageDetails = ({ coverage, publicId }: CoverageDetailsProps) => {
  const coverageTableRows = coverage.groups.map(
    (group) =>
      ({
        name: group.name,
        stats: group.stats,
        previousTestRunId: coverage.previousTestRunId,
        nameLinkUrl: `/tests/${publicId}/coverage/${group.name}/files`,
      }) as CoverageTableRow,
  );

  return (
    <div data-testid="coverage-details">
      <div>
        <Typography className={classes.title} variant="h6">
          Total
        </Typography>
        <OverallCoverageGraphs
          overallStats={coverage.overallStats}
          previousTestRunId={coverage.previousTestRunId}
        />
      </div>
      <CoverageTable
        rows={coverageTableRows}
        pageTitle="Groups"
        groupHeader="Group"
      />
    </div>
  );
};

export default CoverageDetails;
