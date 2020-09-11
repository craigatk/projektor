import * as React from "react";
import { Coverage } from "../model/TestRunModel";
import { makeStyles, Typography } from "@material-ui/core";
import OverallCoverageGraphs from "./OverallCoverageGraphs";
import CoverageGroups from "./CoverageGroups";

interface CoverageDetailsProps {
  coverage: Coverage;
  publicId: string;
}

const useStyles = makeStyles({
  title: {
    paddingLeft: "15px",
  },
});

const CoverageDetails = ({ coverage }: CoverageDetailsProps) => {
  const classes = useStyles({});

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
      <CoverageGroups
        coverageGroups={coverage.groups}
        previousTestRunId={coverage.previousTestRunId}
        pageTitle="Groups"
        groupHeader="Group"
      />
    </div>
  );
};

export default CoverageDetails;
