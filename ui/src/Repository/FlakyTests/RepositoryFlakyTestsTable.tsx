import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import { RepositoryFlakyTests } from "../../model/RepositoryModel";
import { TestCase } from "../../model/TestRunModel";
import CleanLink from "../../Link/CleanLink";
import moment from "moment-timezone";

interface FlakyTestsTableProps {
  flakyTests: RepositoryFlakyTests;
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

interface FlakyTestRow {
  idx: number;
  name: string;
  testCaseIdx: number;
  testSuiteIdx: number;
  failureCount: number;
  failurePercentage: number;
  firstTestCase: TestCase;
  firstCreatedTimestamp: Date;
  latestTestCase: TestCase;
  latestCreatedTimestamp: Date;
}

const RepositoryFlakyTestsTable = ({ flakyTests }: FlakyTestsTableProps) => {
  const sortedFlakyTests = flakyTests.tests.sort(
    (a, b) => b.failureCount - a.failureCount,
  );

  const data: FlakyTestRow[] = useMemo(
    () =>
      sortedFlakyTests.map((flakyTest, idx) => ({
        idx: idx + 1,
        name: flakyTest.testCase.fullName,
        testCaseIdx: flakyTest.testCase.idx,
        testSuiteIdx: flakyTest.testCase.testSuiteIdx,
        failureCount: flakyTest.failureCount,
        failurePercentage: flakyTest.failurePercentage,
        firstTestCase: flakyTest.firstTestCase,
        firstCreatedTimestamp: flakyTest.firstTestCase.createdTimestamp,
        latestTestCase: flakyTest.latestTestCase,
        latestCreatedTimestamp: flakyTest.latestTestCase.createdTimestamp,
      })),
    [sortedFlakyTests],
  );

  const columns = useMemo<MRT_ColumnDef<FlakyTestRow>[]>(
    () => [
      {
        header: "Test case name",
        accessorKey: "name",
        Cell: ({ row }) => (
          <CleanLink
            to={`/tests/${row.original.latestTestCase.publicId}/suite/${row.original.testSuiteIdx}/case/${row.original.testCaseIdx}`}
            data-testid={`flaky-test-case-name-${row.original.idx}`}
          >
            {row.original.name}
          </CleanLink>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Failure percentage",
        accessorKey: "failurePercentage",
        Cell: ({ row }) => (
          <span
            data-testid={`flaky-test-case-failure-percentage-${row.original.idx}`}
          >
            {row.original.failurePercentage}%
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Failure count",
        accessorKey: "failureCount",
        Cell: ({ row }) => (
          <span
            data-testid={`flaky-test-case-failure-count-${row.original.idx}`}
          >
            {row.original.failureCount}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "First failure",
        accessorKey: "firstCreatedTimestamp",
        Cell: ({ row }) => (
          <CleanLink
            to={`/tests/${row.original.firstTestCase.publicId}/suite/${row.original.firstTestCase.testSuiteIdx}/case/${row.original.firstTestCase.idx}`}
            data-testid={`flaky-test-case-first-failure-${row.original.idx}`}
          >
            {moment(row.original.firstCreatedTimestamp).format(
              "MMM Do YYYY, h:mm a",
            )}
          </CleanLink>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Latest failure",
        accessorKey: "latestCreatedTimestamp",
        Cell: ({ row }) => (
          <CleanLink
            to={`/tests/${row.original.latestTestCase.publicId}/suite/${row.original.latestTestCase.testSuiteIdx}/case/${row.original.latestTestCase.idx}`}
            data-testid={`flaky-test-case-latest-failure-${row.original.idx}`}
          >
            {moment(row.original.latestCreatedTimestamp).format(
              "MMM Do YYYY, h:mm a",
            )}
          </CleanLink>
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
    <div data-testid="repository-flaky-tests-table">
      <MaterialReactTable table={table} />
    </div>
  );
};

export default RepositoryFlakyTestsTable;
