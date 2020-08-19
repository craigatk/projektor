import * as React from "react";
import { CoverageStat } from "../model/TestRunModel";
import HSBar from "react-horizontal-stacked-bar-chart";
import { makeStyles, Typography } from "@material-ui/core";

interface CoverageGraphProps {
  coverageStat: CoverageStat;
  type: string;
}

const useStyles = makeStyles({
  wrapper: {
    marginLeft: "10px",
    marginRight: "25px",
    display: "inline-block",
  },
  label: {
    marginBottom: "10px",
    display: "inline-block",
  },
});

const CoverageGraph = ({ coverageStat, type }: CoverageGraphProps) => {
  const classes = useStyles({});

  if (coverageStat.total > 0) {
    return (
      <div className={classes.wrapper}>
        <Typography
          className={classes.label}
          data-testid={`coverage-graph-title-${type.toLowerCase()}`}
        >
          {type} {coverageStat.coveredPercentage}%
        </Typography>
        <HSBar // https://www.npmjs.com/package/react-horizontal-stacked-bar-chart
          data={[
            {
              value: coverageStat.covered,
              description: `${coverageStat.covered} Covered`,
              color: "rgb(0,255,0)",
            },
            {
              value: coverageStat.missed,
              description: `${coverageStat.missed} Missed`,
              color: "red",
            },
          ]}
          showTextDown
          height={25}
        />
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageGraph;
