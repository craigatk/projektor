import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import { TestSuite } from "../model/TestRunModel";
import CleanLink from "../Link/CleanLink";
import {
  anyTestSuiteHasGroupName,
  fullTestSuiteName,
} from "../model/TestSuiteHelpers";

interface TestSuiteListProps {
  publicId: string;
  testSuites: TestSuite[];
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

interface TestSuiteRow {
  idx: number;
  fileName?: string;
  name: string;
  group: string;
  passed: number;
  failed: number;
  duration: number;
}

const TestSuiteList = ({ publicId, testSuites }: TestSuiteListProps) => {
  const hasFileName = testSuites?.some((suite) => suite.fileName);
  const hasGroupName = testSuites && anyTestSuiteHasGroupName(testSuites);

  const data: TestSuiteRow[] = useMemo(
    () =>
      testSuites?.map((testSuite) => ({
        fileName: testSuite.fileName,
        name: fullTestSuiteName(testSuite),
        group: testSuite.groupName || "",
        passed: testSuite.passingCount,
        failed: testSuite.failureCount,
        duration: testSuite.duration,
        idx: testSuite.idx,
      })) ?? [],
    [testSuites],
  );

  const columns = useMemo<MRT_ColumnDef<TestSuiteRow>[]>(() => {
    const cols: MRT_ColumnDef<TestSuiteRow>[] = [
      {
        header: "Test Suite",
        accessorKey: "name",
        Cell: ({ row }) => (
          <CleanLink
            to={`/tests/${publicId}/suite/${row.original.idx}/`}
            data-testid={`test-suite-class-name-${row.original.idx}`}
          >
            {row.original.name}
          </CleanLink>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
    ];

    if (hasFileName) {
      cols.push({
        header: "Test File",
        accessorKey: "fileName",
        Cell: ({ row }) => (
          <CleanLink
            to={`/tests/${publicId}/suite/${row.original.idx}/`}
            data-testid={`test-suite-file-name-${row.original.idx}`}
          >
            {row.original.fileName}
          </CleanLink>
        ),
      });
    }

    if (hasGroupName) {
      cols.push({
        header: "Group",
        accessorKey: "group",
        Cell: ({ row }) => (
          <span data-testid={`test-suite-group-name-${row.original.idx}`}>
            {row.original.group}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      });
    }

    cols.push(
      {
        header: "Passed",
        accessorKey: "passed",
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Failed",
        accessorKey: "failed",
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Duration",
        accessorKey: "duration",
        Cell: ({ row }) => <>{row.original.duration}s</>,
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
    );

    return cols;
  }, [publicId, hasFileName, hasGroupName]);

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

  if (!testSuites) {
    return null;
  }

  return (
    <div data-testid="test-suite-list">
      <MaterialReactTable table={table} />
    </div>
  );
};

export default TestSuiteList;
