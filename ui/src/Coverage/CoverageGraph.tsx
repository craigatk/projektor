import * as React from "react";
import { CoverageStat } from "../model/TestRunModel";
import { makeStyles, Typography } from "@material-ui/core";
import CoveragePercentage from "./CoveragePercentage";
import CleanLink from "../Link/CleanLink";
import CoverageGraphImpl from "./CoverageGraphImpl";

interface CoverageGraphProps {
  coverageStat: CoverageStat;
  type: string;
  height: number;
  inline: boolean;
  coveredPercentageLink?: string;
  previousTestRunId?: string;
  testIdPrefix?: string;
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
  coveredPercentageLink,
  previousTestRunId,
  testIdPrefix,
}: CoverageGraphProps) => {
  const classes = useStyles({ inline });

  if (coverageStat.total > 0) {
    return (
      <div className={classes.wrapper}>
        {!inline && (
          <Typography
            className={classes.label}
            data-testid={`coverage-graph-title-${type.toLowerCase()}`}
          >
            {type}{" "}
            <CoveragePercentage
              coverageStat={coverageStat}
              previousTestRunId={previousTestRunId}
            />
          </Typography>
        )}
        {coveredPercentageLink && (
          <CleanLink
            to={coveredPercentageLink}
            data-testid={`${testIdPrefix}-covered-percentage-link`}
          >
            <CoverageGraphImpl
              coverageStat={coverageStat}
              height={height}
              inline={inline}
              previousTestRunId={previousTestRunId}
              testIdPrefix={testIdPrefix}
            />
          </CleanLink>
        )}
        {!coveredPercentageLink && (
          <CoverageGraphImpl
            coverageStat={coverageStat}
            height={height}
            inline={inline}
            previousTestRunId={previousTestRunId}
            testIdPrefix={testIdPrefix}
          />
        )}
      </div>
    );
  } else {
    return null;
  }
};

export default CoverageGraph;
