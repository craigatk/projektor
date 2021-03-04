import * as React from "react";
import { ResultsProcessingFailure } from "../../model/AdminModel";
import MaterialTable from "material-table";
import moment from "moment";
import { Link, Tooltip } from "@material-ui/core";
import FileCopyOutlinedIcon from "@material-ui/icons/FileCopyOutlined";
import { CopyToClipboard } from "react-copy-to-clipboard";
import { makeStyles } from "@material-ui/styles";

interface AdminFailuresTableProps {
  failures: ResultsProcessingFailure[];
}

const useStyles = makeStyles(() => ({
  copyLink: {
    cursor: "pointer",
  },
}));

const headerStyle = {
  paddingTop: "8px",
  paddingBottom: "8px",
};

const cellStyle = {
  padding: "6px 24px 6px 16px",
};

const AdminFailuresTable = ({ failures }: AdminFailuresTableProps) => {
  const classes = useStyles({});

  return (
    <div data-testid="admin-failures-table">
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: "ID",
            field: "id",
            render: (rowData) => (
              <span data-testid={`admin-failures-id-${rowData.id}`}>
                {rowData.id}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Failure message",
            field: "failureMessage",
            render: (rowData) => (
              <span data-testid={`admin-failures-message-${rowData.id}`}>
                {rowData.failureMessage}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Results body type",
            field: "bodyType",
            render: (rowData) => (
              <span data-testid={`admin-failures-body-type-${rowData.id}`}>
                {rowData.bodyType}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Results body",
            field: "body",
            render: (rowData) => (
              <span
                data-testid={`admin-failures-body-${rowData.id}`}
                className={classes.copyLink}
              >
                <Tooltip title="Copy response body to clipboard">
                  <CopyToClipboard text={rowData.body}>
                    <Link
                      data-testid={`admin-failures-body-copy-${rowData.id}`}
                    >
                      Copy body to clipboard{" "}
                      <FileCopyOutlinedIcon fontSize="small" />
                    </Link>
                  </CopyToClipboard>
                </Tooltip>
              </span>
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Created",
            field: "createdTimestamp",
            render: (rowData) => (
              <span
                data-testid={`admin-failures-created-timestamp-${rowData.id}`}
              >
                {moment(rowData.createdTimestamp).format(
                  "MMMM Do YYYY, h:mm:ss a"
                )}
              </span>
            ),
            cellStyle,
            headerStyle,
          },
        ]}
        data={failures.map((failure) => ({
          id: failure.id,
          body: failure.body,
          bodyType: failure.bodyType,
          createdTimestamp: failure.createdTimestamp,
          failureMessage: failure.failureMessage,
        }))}
      />
    </div>
  );
};

export default AdminFailuresTable;
