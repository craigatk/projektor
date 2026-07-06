import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import {
  CoverageFiles,
  CoverageStat,
  TestRunGitMetadata,
} from "../model/TestRunModel";
import CoverageGraph from "./CoverageGraph";
import { Typography } from "@mui/material";
import GitHubFileLink from "../VersionControl/GitHubFileLink";
import CoverageFileMissedLines from "./CoverageFileMissedLines";

interface CoverageFilesTableProps {
  coverageFiles: CoverageFiles;
  coverageGroupName: string;
  gitMetadata?: TestRunGitMetadata;
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
  fontSize: "0.75em",
};

const calculateFullName = (directoryName: string, fileName: string): string =>
  directoryName.replace(".", "/").replace("\\", "/") + "/" + fileName;

interface CoverageFileRow {
  fileName: string;
  directoryName: string;
  fullName: string;
  filePath: string;
  lineStat: CoverageStat;
  lineCoveredPercentage: number;
  branchStat: CoverageStat;
  branchCoveredPercentage: number;
  missedLines: number[];
  partialLines: number[];
  idx: number;
}

const CoverageFilesTable = ({
  coverageFiles,
  coverageGroupName,
  gitMetadata,
}: CoverageFilesTableProps) => {
  const data: CoverageFileRow[] = useMemo(() => {
    if (
      !coverageFiles ||
      !coverageFiles.files ||
      coverageFiles.files.length === 0
    ) {
      return [];
    }

    const rows = coverageFiles.files
      .filter((file) => file.stats.lineStat.total > 0)
      .map((file) => ({
        fileName: file.fileName,
        directoryName: file.directoryName,
        fullName: calculateFullName(file.directoryName, file.fileName),
        filePath: file.filePath,
        lineStat: file.stats.lineStat,
        lineCoveredPercentage: file.stats.lineStat.coveredPercentage,
        branchStat: file.stats.branchStat,
        branchCoveredPercentage: file.stats.branchStat.coveredPercentage,
        missedLines: file.missedLines,
        partialLines: file.partialLines,
      }));

    return rows
      .sort((a, b) => a.lineStat.coveredPercentage - b.lineStat.coveredPercentage)
      .map((row, idx) => ({
        ...row,
        idx: idx + 1,
      }));
  }, [coverageFiles]);

  const columns = useMemo<MRT_ColumnDef<CoverageFileRow>[]>(
    () => [
      {
        header: "File",
        accessorKey: "fullName",
        Cell: ({ row }) => (
          <Typography
            data-testid={`coverage-file-name-${row.original.idx}`}
            variant="caption"
          >
            <GitHubFileLink
              gitMetadata={gitMetadata}
              filePath={row.original.filePath}
              linkText={row.original.fullName}
              testId={`coverage-file-${row.original.idx}-file-name-link`}
            />
          </Typography>
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
            height={10}
            inline={true}
            testIdPrefix={`coverage-file-line-coverage-row-${row.original.idx}`}
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
            height={10}
            inline={true}
            testIdPrefix={`branch-coverage-row-${row.original.idx}`}
          />
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Missed lines",
        accessorKey: "missedLines",
        Cell: ({ row }) => (
          <CoverageFileMissedLines
            missedLines={row.original.missedLines}
            filePath={row.original.filePath}
            fileIdx={row.original.idx}
            gitMetadata={gitMetadata}
          />
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Partial lines",
        accessorKey: "partialLines",
        Cell: ({ row }) => (
          <Typography
            data-testid={`coverage-file-partial-lines-${row.original.idx}`}
            variant="caption"
          >
            {row.original.partialLines.map((partialLine, idx) => (
              <span key={partialLine}>
                <GitHubFileLink
                  gitMetadata={gitMetadata}
                  filePath={row.original.filePath}
                  linkText={partialLine.toString()}
                  lineNumber={partialLine}
                  testId={`coverage-file-${row.original.idx}-partial-line-link-${partialLine}`}
                />
                {idx < row.original.partialLines.length - 1 && <span>, </span>}
              </span>
            ))}
          </Typography>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
    ],
    [gitMetadata],
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

  if (
    !coverageFiles ||
    !coverageFiles.files ||
    coverageFiles.files.length === 0
  ) {
    return (
      <div>
        <Typography>
          No file-level coverage found for {coverageGroupName}
        </Typography>
      </div>
    );
  }

  return (
    <div>
      <MaterialReactTable table={table} />
    </div>
  );
};

export default CoverageFilesTable;
