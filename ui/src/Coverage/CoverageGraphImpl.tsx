import * as React from "react";
import HSBar from "react-horizontal-stacked-bar-chart";
import { CoverageStat } from "../model/TestRunModel";
import CoveragePercentage from "./CoveragePercentage";

interface CoverageGraphImplProps {
  coverageStat: CoverageStat;
  height: number;
  inline: boolean;
  previousTestRunId?: string;
  testIdPrefix?: string;
}

const CoverageGraphImpl = ({
  coverageStat,
  height,
  inline,
  previousTestRunId,
  testIdPrefix,
}: CoverageGraphImplProps) => {
  const coveredDescription = inline ? (
    <span>
      {coverageStat.covered} Covered (
      <CoveragePercentage
        coverageStat={coverageStat}
        previousTestRunId={previousTestRunId}
        testId={`${testIdPrefix}-covered-percentage`}
      />
      )
    </span>
  ) : (
    <span>{coverageStat.covered} Covered</span>
  );

  return (
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
  );
};

export default CoverageGraphImpl;
