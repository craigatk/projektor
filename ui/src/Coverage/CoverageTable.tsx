import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import classes from "./CoverageTable.module.css";
import CoverageGraph from "./CoverageGraph";
import { Typography } from "@mui/material";
import CleanLink from "../Link/CleanLink";
import CoverageTableRow from "./CoverageTableRow";
import { CoverageStat } from "../model/TestRunModel";

interface CoverageTableProps {
  rows: CoverageTableRow[];
  pageTitle: string;
  groupHeader: string;
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

interface CoverageRow {
  name: string;
  lineStat: CoverageStat;
  lineCoveredPercentage: number;
  statementStat: CoverageStat;
  statementCoveredPercentage: number;
  branchStat: CoverageStat;
  branchCoveredPercentage: number;
  coveredPercentageLink?: string;
  previousTestRunId?: string;
  nameLinkUrl?: string;
  idx: number;
}

const CoverageTable = ({
  rows,
  pageTitle,
  groupHeader,
}: CoverageTableProps) => {
  const sortedRows = rows.sort(
    (a, b) =>
      a.stats.lineStat.coveredPercentage - b.stats.lineStat.coveredPercentage,
  );

  const data: CoverageRow[] = useMemo(
    () =>
      sortedRows.map((row, idx) => ({
        name: row.name,
        lineStat: row.stats.lineStat,
        lineCoveredPercentage: row.stats.lineStat.coveredPercentage,
        statementStat: row.stats.statementStat,
        statementCoveredPercentage: row.stats.statementStat.coveredPercentage,
        branchStat: row.stats.branchStat,
        branchCoveredPercentage: row.stats.branchStat.coveredPercentage,
        coveredPercentageLink: row.coveredPercentageLink,
        previousTestRunId: row.previousTestRunId,
        nameLinkUrl: row.nameLinkUrl,
        idx: idx + 1,
      })),
    [sortedRows],
  );

  const columns = useMemo<MRT_ColumnDef<CoverageRow>[]>(
    () => [
      {
        header: groupHeader,
        accessorKey: "name",
        Cell: ({ row }) =>
          row.original.nameLinkUrl ? (
            <CleanLink
              to={row.original.nameLinkUrl}
              data-testid={`coverage-name-${row.original.idx}`}
            >
              {row.original.name}
            </CleanLink>
          ) : (
            <span data-testid={`coverage-name-${row.original.idx}`}>
              {row.original.name}
            </span>
          ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Line",
        accessorKey: "lineCoveredPercentage",
        Cell: ({ row }) => (
          <CoverageGraph
            coverageStat={row.original.lineStat}
            type="Line"
            height={15}
            inline={true}
            coveredPercentageLink={row.original.coveredPercentageLink}
            previousTestRunId={row.original.previousTestRunId}
            testIdPrefix={`line-coverage-row-${row.original.idx}`}
          />
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Branch",
        accessorKey: "branchCoveredPercentage",
        Cell: ({ row }) => (
          <CoverageGraph
            coverageStat={row.original.branchStat}
            type="Branch"
            height={15}
            inline={true}
            coveredPercentageLink={row.original.coveredPercentageLink}
            previousTestRunId={row.original.previousTestRunId}
            testIdPrefix={`branch-coverage-row-${row.original.idx}`}
          />
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Statement",
        accessorKey: "statementCoveredPercentage",
        Cell: ({ row }) => (
          <CoverageGraph
            coverageStat={row.original.statementStat}
            type="Statement"
            height={15}
            inline={true}
            coveredPercentageLink={row.original.coveredPercentageLink}
            previousTestRunId={row.original.previousTestRunId}
            testIdPrefix={`statement-coverage-row-${row.original.idx}`}
          />
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
    ],
    [groupHeader],
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
    <div>
      <Typography className={classes.title} variant="h6">
        {pageTitle}
      </Typography>
      <MaterialReactTable table={table} />
    </div>
  );
};

export default CoverageTable;
