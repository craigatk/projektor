import * as React from "react";
import { CoverageGroup } from "../model/TestRunModel";
import MaterialTable from "material-table";
import CoverageGraph from "./CoverageGraph";
import { makeStyles, Typography } from "@material-ui/core";

interface CoverageGroupsProps {
  coverageGroups: CoverageGroup[];
  previousTestRunId?: string;
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

const CoverageGroups = ({
  coverageGroups,
  previousTestRunId,
}: CoverageGroupsProps) => {
  const classes = useStyles({});

  return (
    <div>
      <Typography className={classes.title} variant="h6">
        Groups
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
            title: "Test Group",
            field: "name",
            render: (rowData) => (
              <span data-testid={`name-${rowData.name}`}>{rowData.name}</span>
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
                previousTestRunId={previousTestRunId}
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
                previousTestRunId={previousTestRunId}
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
                previousTestRunId={previousTestRunId}
              />
            ),
            cellStyle,
            headerStyle,
          },
        ]}
        data={coverageGroups.map((coverageGroup) => ({
          name: coverageGroup.name,
          lineStat: coverageGroup.stats.lineStat,
          lineCoveredPercentage: coverageGroup.stats.lineStat.coveredPercentage,
          statementStat: coverageGroup.stats.statementStat,
          statementCoveredPercentage:
            coverageGroup.stats.lineStat.coveredPercentage,
          branchStat: coverageGroup.stats.branchStat,
          branchCoveredPercentage:
            coverageGroup.stats.lineStat.coveredPercentage,
        }))}
      />
    </div>
  );
};

export default CoverageGroups;
