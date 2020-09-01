import * as React from "react";
import { CoverageStat } from "../model/TestRunModel";
import { makeStyles, Tooltip } from "@material-ui/core";

interface CoveragePercentageProps {
  coverageStat: CoverageStat;
}

const useStyles = makeStyles({
  positive: {
    color: "green",
  },
  negative: {
    color: "red",
  },
});

const CoveragePercentage = ({ coverageStat }: CoveragePercentageProps) => {
  const classes = useStyles({});
  if (coverageStat.coveredPercentageDelta) {
    if (coverageStat.coveredPercentageDelta > 0) {
      return (
        <span>
          {coverageStat.coveredPercentage}%{" "}
          <Tooltip
            title="Coverage percentage increased between this run and the previous main branch run in this repo"
            placement="top"
          >
            <span className={classes.positive}>
              +{coverageStat.coveredPercentageDelta}%
            </span>
          </Tooltip>
        </span>
      );
    } else {
      return (
        <span>
          {coverageStat.coveredPercentage}%{" "}
          <Tooltip
            title="Coverage percentage decreased between this run and the previous main branch run in this repo"
            placement="top"
          >
            <span className={classes.negative}>
              {coverageStat.coveredPercentageDelta}%
            </span>
          </Tooltip>
        </span>
      );
    }
  } else {
    return <span>{coverageStat.coveredPercentage}%</span>;
  }
};

export default CoveragePercentage;
