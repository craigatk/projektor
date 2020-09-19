import * as React from "react";
import { CoverageStats } from "../model/TestRunModel";
import MaterialTable from "material-table";
import CoverageGraph from "./CoverageGraph";
import { makeStyles, Typography } from "@material-ui/core";
import CleanLink from "../Link/CleanLink";

interface CoverageTableRow {
  name: string;
  stats: CoverageStats;
  coveredPercentageLink?: string;
  previousTestRunId?: string;
  nameLinkUrl?: string;
}

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

const useStyles = makeStyles({
  title: {
    paddingLeft: "15px",
  },
});

const CoverageTable = ({
  rows,
  pageTitle,
  groupHeader,
}: CoverageTableProps) => {
  const classes = useStyles({});

  const sortedRows = rows.sort(
    (a, b) =>
      a.stats.lineStat.coveredPercentage - b.stats.lineStat.coveredPercentage
  );

  return (
    <div>
      <Typography className={classes.title} variant="h6">
        {pageTitle}
      </Typography>
      <MaterialTable
        title=""
        style={{ boxShadow: "none" }}
        options={{
          sorting: true,
          paging: false,
        }}
        columns={[
          {
            title: groupHeader,
            field: "name",
            render: (rowData) =>
              rowData.nameLinkUrl ? (
                <CleanLink
                  to={rowData.nameLinkUrl}
                  data-testid={`coverage-name-${rowData.idx}`}
                >
                  {rowData.name}
                </CleanLink>
              ) : (
                <span data-testid={`coverage-name-${rowData.idx}`}>
                  {rowData.name}
                </span>
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
                height={15}
                inline={true}
                coveredPercentageLink={rowData.coveredPercentageLink}
                previousTestRunId={rowData.previousTestRunId}
                testIdPrefix={`line-coverage-row-${rowData.idx}`}
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
                height={15}
                inline={true}
                coveredPercentageLink={rowData.coveredPercentageLink}
                previousTestRunId={rowData.previousTestRunId}
                testIdPrefix={`branch-coverage-row-${rowData.idx}`}
              />
            ),
            cellStyle,
            headerStyle,
          },
          {
            title: "Statement",
            field: "statementCoveredPercentage",
            render: (rowData) => (
              <CoverageGraph
                coverageStat={rowData.statementStat}
                type="Statement"
                height={15}
                inline={true}
                coveredPercentageLink={rowData.coveredPercentageLink}
                previousTestRunId={rowData.previousTestRunId}
                testIdPrefix={`statement-coverage-row-${rowData.idx}`}
              />
            ),
            cellStyle,
            headerStyle,
          },
        ]}
        data={sortedRows.map((row, idx) => ({
          name: row.name,
          lineStat: row.stats.lineStat,
          lineCoveredPercentage: row.stats.lineStat.coveredPercentage,
          statementStat: row.stats.statementStat,
          statementCoveredPercentage: row.stats.lineStat.coveredPercentage,
          branchStat: row.stats.branchStat,
          branchCoveredPercentage: row.stats.lineStat.coveredPercentage,
          coveredPercentageLink: row.coveredPercentageLink,
          previousTestRunId: row.previousTestRunId,
          nameLinkUrl: row.nameLinkUrl,
          idx: idx + 1,
        }))}
      />
    </div>
  );
};

export default CoverageTable;

export { CoverageTableRow };
