import * as React from "react";
import { Coverage } from "../model/TestRunModel";
import { makeStyles, Typography } from "@material-ui/core";
import OverallCoverageGraphs from "./OverallCoverageGraphs";
import CoverageTable, { CoverageTableRow } from "./CoverageTable";

interface CoverageDetailsProps {
  coverage: Coverage;
  publicId: string;
}

const useStyles = makeStyles({
  title: {
    paddingLeft: "15px",
  },
});

const CoverageDetails = ({ coverage, publicId }: CoverageDetailsProps) => {
  const classes = useStyles({});

  const coverageTableRows = coverage.groups.map(
    (group) =>
      ({
        name: group.name,
        stats: group.stats,
        previousTestRunId: coverage.previousTestRunId,
        nameLinkUrl: `/tests/${publicId}/coverage/${group.name}/files`,
      } as CoverageTableRow)
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
