import * as React from "react";
import { PerformanceResult } from "../model/TestRunModel";
import MaterialTable from "material-table";
import { Typography } from "@material-ui/core";

interface PerformanceResultsTableProps {
  performanceResults: PerformanceResult[];
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

const PerformanceResultsTable = ({
  performanceResults,
}: PerformanceResultsTableProps) => {
  return (
    <div data-testid="performance-results-table">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: "Test name",
            field: "name",
            render: (rowData) => (
              <span data-testid={`performance-result-name-${rowData.idx}`}>
                {rowData.name}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Average",
            field: "average",
            render: (rowData) => (
              <span data-testid={`performance-result-average-${rowData.idx}`}>
                {rowData.average}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "p95",
            field: "p95",
            render: (rowData) => (
              <span data-testid={`performance-result-p95-${rowData.idx}`}>
                {rowData.p95}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Max",
            field: "maximum",
            render: (rowData) => (
              <span data-testid={`performance-result-maximum-${rowData.idx}`}>
                {rowData.maximum}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Requests per second",
            field: "requestsPerSecond",
            render: (rowData) => (
              <span
                data-testid={`performance-result-requests-per-second-${rowData.idx}`}
              >
                {rowData.requestsPerSecond}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Request count",
            field: "requestCount",
            render: (rowData) => (
              <span
                data-testid={`performance-result-request-count-${rowData.idx}`}
              >
                {rowData.requestCount}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
        ]}
        data={performanceResults.map((result, idx) => ({
          ...result,
          idx: idx + 1,
        }))}
      />
    </div>
  );
};

export default PerformanceResultsTable;
