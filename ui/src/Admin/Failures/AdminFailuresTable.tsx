import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import classes from "./AdminFailuresTable.module.css";
import { ResultsProcessingFailure } from "../../model/AdminModel";
import moment from "moment";
import { Link, Tooltip } from "@mui/material";
import FileCopyOutlinedIcon from "@mui/icons-material/FileCopyOutlined";
import { CopyToClipboard } from "react-copy-to-clipboard";

interface AdminFailuresTableProps {
  failures: ResultsProcessingFailure[];
}

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

interface AdminFailureRow {
  id: string;
  body: string;
  bodyType: string;
  createdTimestamp: Date;
  failureMessage: string;
}

const AdminFailuresTable = ({ failures }: AdminFailuresTableProps) => {
  const data: AdminFailureRow[] = useMemo(
    () =>
      failures.map((failure) => ({
        id: failure.id,
        body: failure.body,
        bodyType: failure.bodyType,
        createdTimestamp: failure.createdTimestamp,
        failureMessage: failure.failureMessage,
      })),
    [failures],
  );

  const columns = useMemo<MRT_ColumnDef<AdminFailureRow>[]>(
    () => [
      {
        header: "ID",
        accessorKey: "id",
        Cell: ({ row }) => (
          <span data-testid={`admin-failures-id-${row.original.id}`}>
            {row.original.id}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Failure message",
        accessorKey: "failureMessage",
        Cell: ({ row }) => (
          <span data-testid={`admin-failures-message-${row.original.id}`}>
            {row.original.failureMessage}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Results body type",
        accessorKey: "bodyType",
        Cell: ({ row }) => (
          <span data-testid={`admin-failures-body-type-${row.original.id}`}>
            {row.original.bodyType}
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Results body",
        accessorKey: "body",
        Cell: ({ row }) => (
          <span
            data-testid={`admin-failures-body-${row.original.id}`}
            className={classes.copyLink}
          >
            <Tooltip title="Copy response body to clipboard">
              <span>
                <CopyToClipboard text={row.original.body}>
                  <Link data-testid={`admin-failures-body-copy-${row.original.id}`}>
                    Copy body to clipboard <FileCopyOutlinedIcon fontSize="small" />
                  </Link>
                </CopyToClipboard>
              </span>
            </Tooltip>
          </span>
        ),
        muiTableBodyCellProps: { sx: cellStyle },
        muiTableHeadCellProps: { sx: headerStyle },
      },
      {
        header: "Created",
        accessorKey: "createdTimestamp",
        Cell: ({ row }) => (
          <span
            data-testid={`admin-failures-created-timestamp-${row.original.id}`}
          >
            {moment(row.original.createdTimestamp).format(
              "MMMM Do YYYY, h:mm:ss a",
            )}
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
    enablePagination: true,
    enableColumnActions: false,
    enableColumnFilters: false,
    enableDensityToggle: false,
    enableFullScreenToggle: false,
    enableHiding: false,
    initialState: {
      pagination: { pageSize: 10, pageIndex: 0 },
    },
    muiTablePaperProps: {
      elevation: 0,
      sx: { boxShadow: "none" },
    },
  });

  return (
    <div data-testid="admin-failures-table">
      <MaterialReactTable table={table} />
    </div>
  );
};

export default AdminFailuresTable;
