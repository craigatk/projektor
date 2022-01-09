import * as React from "react";
import { Attachments } from "../model/TestRunModel";
import MaterialTable from "@material-table/core";
import prettyBytes from "pretty-bytes";

interface AttachmentsListProps {
  publicId: string;
  attachments: Attachments;
}

const AttachmentsList = ({ publicId, attachments }: AttachmentsListProps) => {
  return (
    <div data-testid="attachments-list">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: "File name",
            field: "fileName",
            render: (rowData) => (
              <span data-testid={`attachment-file-name-${rowData.fileName}`}>
                <a href={`/run/${publicId}/attachments/${rowData.fileName}`}>
                  {rowData.fileName}
                </a>
              </span>
            ),
          },
          {
            title: "Size",
            field: "fileSize",
            render: (rowData) => (
              <span data-testid={`attachment-file-size-${rowData.fileName}`}>
                {rowData.fileSize ? prettyBytes(rowData.fileSize) : ""}
              </span>
            ),
          },
        ]}
        data={attachments.attachments.map((attachment) => ({
          fileName: attachment.fileName,
          fileSize: attachment.fileSize,
        }))}
      />
    </div>
  );
};

export default AttachmentsList;
