import * as React from "react";
import { useMemo } from "react";
import {
  MaterialReactTable,
  type MRT_ColumnDef,
  useMaterialReactTable,
} from "material-react-table";
import { Attachments } from "../model/TestRunModel";
import AttachmentSize from "./AttachmentSize";

interface AttachmentsListProps {
  publicId: string;
  attachments: Attachments;
}

interface AttachmentRow {
  fileName: string;
  fileSize: number;
}

const AttachmentsList = ({ publicId, attachments }: AttachmentsListProps) => {
  const data: AttachmentRow[] = useMemo(
    () =>
      attachments.attachments.map((attachment) => ({
        fileName: attachment.fileName,
        fileSize: attachment.fileSize,
      })),
    [attachments],
  );

  const columns = useMemo<MRT_ColumnDef<AttachmentRow>[]>(
    () => [
      {
        header: "File name",
        accessorKey: "fileName",
        Cell: ({ row }) => (
          <span data-testid={`attachment-file-name-${row.original.fileName}`}>
            <a href={`/run/${publicId}/attachments/${row.original.fileName}`}>
              {row.original.fileName}
            </a>
          </span>
        ),
      },
      {
        header: "Size",
        accessorKey: "fileSize",
        Cell: ({ row }) => (
          <span data-testid={`attachment-file-size-${row.original.fileName}`}>
            <AttachmentSize fileSize={row.original.fileSize} />
          </span>
        ),
      },
    ],
    [publicId],
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
    <div data-testid="attachments-list">
      <MaterialReactTable table={table} />
    </div>
  );
};

export default AttachmentsList;
