import * as React from "react";
import { CoverageStat } from "../model/TestRunModel";
import HSBar from "react-horizontal-stacked-bar-chart";
import { makeStyles, Typography } from "@material-ui/core";

interface CoverageGraphProps {
  coverageStat: CoverageStat;
  type: string;
  height: number;
  inline: boolean;
}

const useStyles = makeStyles({
  wrapper: (props) => ({
    // @ts-ignore
    marginRight: props.inline ? "10px" : "50px",
    display: "inline-block",
  }),
  label: {
    marginBottom: "10px",
    display: "inline-block",
  },
});

const CoverageGraph = ({
  coverageStat,
  type,
  height,
  inline,
}: CoverageGraphProps) => {
  const classes = useStyles({ inline });

  const coveredDescription = inline
    ? `${coverageStat.covered} Covered (${coverageStat.coveredPercentage}%)`
    : `${coverageStat.covered} Covered`;

  if (coverageStat.total > 0) {
    return (
      <div className={classes.wrapper}>
        {!inline && (
          <Typography
            className={classes.label}
            data-testid={`coverage-graph-title-${type.toLowerCase()}`}
          >
            {type} {coverageStat.coveredPercentage}%
          </Typography>
        )}
        <HSBar // https://www.npmjs.com/package/react-horizontal-stacked-bar-chart
          data={[
            {
              value: coverageStat.covered,
              description: coveredDescription,
              color: "rgb(0,255,0)",
            },
            {
              value: coverageStat.missed,
              description: `${coverageStat.missed}`,
              color: "red",
            },
          ]}
          showTextDown
          height={height}
        />
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageGraph;
