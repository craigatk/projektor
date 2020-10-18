import * as React from "react";
import { RepositoryTimeline } from "../../model/RepositoryModel";
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
import RepositoryTimelineGraphTooltip from "./RepositoryTimelineGraphTooltip";
import RepositoryGraphActiveDot from "../Graph/RepositoryGraphActiveDot";
import RepositoryGraphDot from "../Graph//RepositoryGraphDot";
import { formatSecondsDurationWithoutMS } from "../../dateUtils/dateUtils";

interface RepositoryTimelineGraphProps {
  timeline: RepositoryTimeline;
  graphWidth?: number;
}

const RepositoryTimelineGraph = ({
  timeline,
  graphWidth,
}: RepositoryTimelineGraphProps) => {
  const data = timeline.timelineEntries.map((entry) => ({
    date: moment.utc(entry.createdTimestamp).format("YYYY-MM-DD hh:mm:ss"),
    createdTimestamp: entry.createdTimestamp,
    publicId: entry.publicId,
    duration: entry.cumulativeDuration,
    totalTestCount: entry.totalTestCount,
    testAverageDuration: entry.testAverageDuration,
  }));

  const xAxisTickFormatter = (value) => moment(value).format("MMM Do YYYY");

  const durationYAxisFormatter = (value) =>
    formatSecondsDurationWithoutMS(value);

  return (
    <div data-testid="repository-timeline-graph">
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
          <YAxis
            yAxisId="duration"
            orientation="left"
            tickFormatter={durationYAxisFormatter}
          />
          <YAxis yAxisId="totalTestCount" orientation="right" />
          <Legend
            formatter={(value, _) =>
              value === "duration" ? "Test execution time" : "Test count"
            }
          />
          <Tooltip content={<RepositoryTimelineGraphTooltip />} />
          <Line
            type="monotone"
            dataKey="duration"
            stroke="#8884d8"
            activeDot={<RepositoryGraphActiveDot />}
            dot={<RepositoryGraphDot />}
            yAxisId="duration"
          />
          <Line
            type="monotone"
            dataKey="totalTestCount"
            stroke="#64aed8"
            activeDot={<RepositoryGraphActiveDot />}
            dot={<RepositoryGraphDot />}
            yAxisId="totalTestCount"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default RepositoryTimelineGraph;
