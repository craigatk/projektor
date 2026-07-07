import * as React from "react";
import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis } from "recharts";
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

  // Non-inline graphs (e.g. the dashboard summary) have enough width to always
  // show the "Uncovered" label; inline graphs (e.g. table cells) are narrower,
  // so only show it there when the uncovered portion is large enough.
  const missedText =
    !inline || coverageStat.coveredPercentage <= 75
      ? `${coverageStat.missed} Uncovered`
      : `${coverageStat.missed}`;

  const data = [
    {
      name: "coverage",
      covered: coverageStat.covered,
      missed: coverageStat.missed,
    },
  ];

  return (
    <>
      <ResponsiveContainer width="100%" height={height}>
        <BarChart
          layout="vertical"
          data={data}
          margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
        >
          <XAxis type="number" domain={[0, coverageStat.total]} hide />
          <YAxis type="category" dataKey="name" hide />
          <Bar
            dataKey="covered"
            stackId="coverage"
            fill="rgb(0,255,0)"
            isAnimationActive={false}
          />
          <Bar
            dataKey="missed"
            stackId="coverage"
            fill="red"
            isAnimationActive={false}
          />
        </BarChart>
      </ResponsiveContainer>
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          width: "100%",
          gap: "8px",
          justifyContent: "space-between",
        }}
      >
        {coveredDescription}
        {coverageStat.missed > 0 && (
          <span data-testid={`${testIdPrefix}-uncovered-line-count`}>
            {missedText}
          </span>
        )}
      </div>
    </>
  );
};

export default CoverageGraphImpl;
