import * as React from "react";
import { TestRunSummary } from "../model/TestRunModel";
import { Grid, Hidden, List } from "@material-ui/core";
import TestCountList from "../TestCount/TestCountList";
import PageTitle from "../PageTitle";
import TestRunDuration from "./TestRunDuration";
import TestRunDate from "./TestRunDate";
import TestRunCleanupDate from "./TestRunCleanupDate";
import TestRunMessages from "../TestRunMessages/TestRunMessages";

interface DashboardSummaryProps {
  publicId: string;
  testRunSummary: TestRunSummary;
}

const DashboardSummary = ({
  publicId,
  testRunSummary,
}: DashboardSummaryProps) => {
  const {
    totalPassingCount,
    totalFailureCount,
    totalSkippedCount,
    totalTestCount,
    averageDuration,
    cumulativeDuration,
    slowestTestCaseDuration,
    createdTimestamp,
  } = testRunSummary;

  const hasDurationData =
    averageDuration &&
    averageDuration > 0 &&
    cumulativeDuration &&
    cumulativeDuration > 0;

  return (
    <div>
      <PageTitle title="Tests" testid="dashboard-summary-title" />
      <TestRunMessages publicId={publicId} />
      <Grid container>
        <Grid item sm={3} xs={12}>
          <TestCountList
            passedCount={totalPassingCount}
            failedCount={totalFailureCount}
            skippedCount={totalSkippedCount}
            totalCount={totalTestCount}
            horizontal={false}
          />
        </Grid>
        <Hidden xsDown>
          <Grid item sm={3} xs={12}>
            {hasDurationData && (
              <TestRunDuration
                publicId={publicId}
                averageDuration={averageDuration}
                cumulativeDuration={cumulativeDuration}
                slowestTestCaseDuration={slowestTestCaseDuration}
              />
            )}
          </Grid>
        </Hidden>
        <Grid item sm={4} xs={12}>
          <List dense={true}>
            <TestRunDate createdTimestamp={createdTimestamp} />

            <TestRunCleanupDate createdTimestamp={createdTimestamp} />
          </List>
        </Grid>
      </Grid>
    </div>
  );
};

export default DashboardSummary;
