import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import { PerformanceResult } from "../model/TestRunModel";

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

interface PerformanceResultRow extends PerformanceResult {
  idx: number;
}

const PerformanceResultsTable = ({
  performanceResults,
}: PerformanceResultsTableProps) => {
  const data: PerformanceResultRow[] = useMemo(
    () =>
      performanceResults.map((result, idx) => ({
        ...result,
        idx: idx + 1,
      })),
    [performanceResults],
  );

  const columns = useMemo<MRT_ColumnDef<PerformanceResultRow>[]>(
    () => [
      {
        header: "Test name",
        accessorKey: "name",
        Cell: ({ row }) => (
          <span data-testid={`performance-result-name-${row.original.idx}`}>
            {row.original.name}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Average",
        accessorKey: "average",
        Cell: ({ row }) => (
          <span data-testid={`performance-result-average-${row.original.idx}`}>
            {row.original.average} ms
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "p95",
        accessorKey: "p95",
        Cell: ({ row }) => (
          <span data-testid={`performance-result-p95-${row.original.idx}`}>
            {row.original.p95} ms
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Max",
        accessorKey: "maximum",
        Cell: ({ row }) => (
          <span data-testid={`performance-result-maximum-${row.original.idx}`}>
            {row.original.maximum} ms
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Requests per second",
        accessorKey: "requestsPerSecond",
        Cell: ({ row }) => (
          <span
            data-testid={`performance-result-requests-per-second-${row.original.idx}`}
          >
            {row.original.requestsPerSecond}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Request count",
        accessorKey: "requestCount",
        Cell: ({ row }) => (
          <span
            data-testid={`performance-result-request-count-${row.original.idx}`}
          >
            {row.original.requestCount}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
    ],
    [],
  );

  const table = useMaterialReactTable({
    columns,
    data,
    enableSorting: true,
    sortDescFirst: false,
    enableTopToolbar: true,
    enableGlobalFilter: true,
    enableBottomToolbar: false,
    enablePagination: false,
    enableColumnActions: false,
    enableColumnFilters: false,
    enableDensityToggle: false,
    enableFullScreenToggle: false,
    enableHiding: false,
    muiTablePaperProps: {
      elevation: 0,
      sx: { boxShadow: "none" },
    },
  });

  return (
    <div data-testid="performance-results-table">
      <MaterialReactTable table={table} />
    </div>
  );
};

export default PerformanceResultsTable;
