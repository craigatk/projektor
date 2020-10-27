import * as React from "react";
import MaterialTable from "material-table";
import { CoverageFiles } from "../model/TestRunModel";
import CoverageGraph from "./CoverageGraph";
import { Typography } from "@material-ui/core";

interface CoverageFilesTableProps {
  coverageFiles: CoverageFiles;
  coverageGroupName: string;
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

const CoverageFilesTable = ({
  coverageFiles,
  coverageGroupName,
}: CoverageFilesTableProps) => {
  if (coverageFiles && coverageFiles.files && coverageFiles.files.length > 0) {
    const rows = coverageFiles.files
      .filter((file) => file.stats.lineStat.total > 0)
      .map((file) => ({
        fileName: file.fileName,
        directoryName: file.directoryName,
        fullName: calculateFullName(file.directoryName, file.fileName),
        lineStat: file.stats.lineStat,
        lineCoveredPercentage: file.stats.lineStat.coveredPercentage,
        branchStat: file.stats.branchStat,
        branchCoveredPercentage: file.stats.branchStat.coveredPercentage,
        missedLines: file.missedLines,
        partialLines: file.partialLines,
      }));

    const sortedRows = rows
      .sort((a, b) => a.fullName.localeCompare(b.fullName))
      .map((row, idx) => ({
        ...row,
        idx: idx + 1,
      }));

    return (
      <div>
        <MaterialTable
          title=""
          style={{ boxShadow: "none" }}
          options={{
            sorting: true,
            paging: false,
          }}
          columns={[
            {
              title: "File",
              field: "fullName",
              render: (rowData) => (
                <Typography
                  data-testid={`coverage-file-name-${rowData.idx}`}
                  variant="caption"
                >
                  {rowData.fullName}
                </Typography>
              ),
              cellStyle,
              headerStyle,
            },
            {
              title: "Line",
              field: "lineCoveredPercentage",
              render: (rowData) => (
                <CoverageGraph
                  coverageStat={rowData.lineStat}
                  type="Line"
                  height={10}
                  inline={true}
                  testIdPrefix={`coverage-file-line-coverage-row-${rowData.idx}`}
                />
              ),
              cellStyle,
              headerStyle,
            },
            {
              title: "Branch",
              field: "branchCoveredPercentage",
              render: (rowData) => (
                <CoverageGraph
                  coverageStat={rowData.branchStat}
                  type="Branch"
                  height={10}
                  inline={true}
                  testIdPrefix={`branch-coverage-row-${rowData.idx}`}
                />
              ),
              cellStyle,
              headerStyle,
            },
            {
              title: "Missed lines",
              field: "missedLines",
              render: (rowData) => (
                <Typography
                  data-testid={`coverage-file-missed-lines-${rowData.idx}`}
                  variant="caption"
                >
                  {rowData.missedLines.join(", ")}
                </Typography>
              ),
              cellStyle,
              headerStyle,
            },
            {
              title: "Partial lines",
              field: "partialLines",
              render: (rowData) => (
                <Typography
                  data-testid={`coverage-file-partial-lines-${rowData.idx}`}
                  variant="caption"
                >
                  {rowData.partialLines.join(", ")}
                </Typography>
              ),
              cellStyle,
              headerStyle,
            },
          ]}
          data={sortedRows}
        />
      </div>
    );
  } else {
    return (
      <div>
        <Typography>
          No file-level coverage found for {coverageGroupName}
        </Typography>
      </div>
    );
  }
};

export default CoverageFilesTable;
