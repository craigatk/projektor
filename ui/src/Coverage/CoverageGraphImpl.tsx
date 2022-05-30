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

  const data = [
    {
      value: coverageStat.covered,
      description: coveredDescription,
      color: "rgb(0,255,0)",
    },
  ];

  if (coverageStat.missed > 0) {
    // The HSBar component sets a NaN width if the value is 0
    // so don't add that data element if it is 0.

    const missedText =
      coverageStat.coveredPercentage > 50
        ? `${coverageStat.missed}`
        : `${coverageStat.missed} Uncovered`;

    data.push({
      value: coverageStat.missed,
      // @ts-ignore
      description: (
        <span data-testid={`${testIdPrefix}-uncovered-line-count`}>
          {missedText}
        </span>
      ),
      color: "red",
    });
  }

  return (
    <HSBar // https://www.npmjs.com/package/react-horizontal-stacked-bar-chart
      data={data}
      showTextDown
      height={height}
    />
  );
};

export default CoverageGraphImpl;
