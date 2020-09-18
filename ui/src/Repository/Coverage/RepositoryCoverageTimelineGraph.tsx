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
  ResponsiveContainer,
} from "recharts";
import moment from "moment";
import RepositoryCoverageTimelineGraphTooltip from "./RepositoryCoverageTimelineGraphTooltip";
import RepositoryCoverageTimelineGraphActiveDot from "./RepositoryCoverageTimelineGraphActiveDot";
import RepositoryCoverageTimelineGraphDot from "./RepositoryCoverageTimelineGraphDot";

interface RepositoryCoverageTimelineGraphProps {
  coverageTimeline: RepositoryCoverageTimeline;
  graphWidth?: number;
}

const RepositoryCoverageTimelineGraph = ({
  coverageTimeline,
  graphWidth,
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
    <div data-testid="repository-coverage-timeline-graph">
      <ResponsiveContainer width={graphWidth || "100%"} height={300}>
        <LineChart
          data={data}
          margin={{
            top: 30,
            right: 50,
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
            activeDot={<RepositoryCoverageTimelineGraphActiveDot />}
            dot={<RepositoryCoverageTimelineGraphDot />}
          />
          <Line
            type="monotone"
            dataKey="branchValue"
            stroke="#64aed8"
            activeDot={<RepositoryCoverageTimelineGraphActiveDot />}
            dot={<RepositoryCoverageTimelineGraphDot />}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default RepositoryCoverageTimelineGraph;
