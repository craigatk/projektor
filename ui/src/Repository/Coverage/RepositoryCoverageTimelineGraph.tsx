import * as React from "react";
import { RepositoryCoverageTimeline } from "../../model/RepositoryModel";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  Dot,
} from "recharts";
import moment from "moment";
import RepositoryCoverageTimelineGraphTooltip from "./RepositoryCoverageTimelineGraphTooltip";

interface RepositoryCoverageTimelineGraphProps {
  coverageTimeline: RepositoryCoverageTimeline;
}

const RepositoryCoverageTimelineGraph = ({
  coverageTimeline,
}: RepositoryCoverageTimelineGraphProps) => {
  const data = coverageTimeline.timelineEntries.map((entry) => ({
    date: moment.utc(entry.createdTimestamp).format("YYYY-MM-DD hh:mm:ss"),
    publicId: entry.publicId,
    lineValue: entry.coverageStats.lineStat.coveredPercentage,
    branchValue: entry.coverageStats.branchStat.coveredPercentage,
  }));

  const xAxisTickFormatter = (value) => moment(value).format("MMM Do YYYY");

  const yAxisTickFormatter = (value) => `${value}%`;

  return (
    <div data-testid="RepositoryCoverageTimelineGraph">
      <LineChart
        width={800}
        height={300}
        data={data}
        margin={{
          top: 30,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" tickFormatter={xAxisTickFormatter} />
        <YAxis tickFormatter={yAxisTickFormatter} />
        <Legend
          formatter={(value, _) =>
            value === "lineValue" ? "Line coverage" : "Branch coverage"
          }
        />
        <Tooltip content={<RepositoryCoverageTimelineGraphTooltip />} />
        <Line
          type="monotone"
          dataKey="lineValue"
          stroke="#8884d8"
          activeDot={{ r: 8 }}
        />
        <Line
          type="monotone"
          dataKey="branchValue"
          stroke="#64aed8"
          activeDot={{ r: 8 }}
        />
      </LineChart>
    </div>
  );
};

export default RepositoryCoverageTimelineGraph;
