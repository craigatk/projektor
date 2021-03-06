import * as React from "react";
import { TestRunGitMetadata, TestRunSummary } from "../model/TestRunModel";
import { Grid, Hidden, List } from "@material-ui/core";
import TestCountList from "../TestCount/TestCountList";
import PageTitle from "../PageTitle";
import TestRunDuration from "./TestRunDuration";
import TestRunDate from "./TestRunDate";
import TestRunCleanupDate from "./TestRunCleanupDate";
import TestRunMessages from "../TestRunMessages/TestRunMessages";
import GitRepoListItem from "./GitRepoListItem";
import DashboardSummaryItem from "./DashboardSummaryItem";

interface DashboardSummaryProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  gitMetadata?: TestRunGitMetadata;
}

const DashboardSummary = ({
  publicId,
  testRunSummary,
  gitMetadata,
}: DashboardSummaryProps) => {
  const {
    totalPassingCount,
    totalFailureCount,
    totalSkippedCount,
    totalTestCount,
    averageDuration,
    cumulativeDuration,
    wallClockDuration,
    slowestTestCaseDuration,
    createdTimestamp,
  } = testRunSummary;

  const hasDurationData =
    !!averageDuration &&
    averageDuration > 0 &&
    !!cumulativeDuration &&
    cumulativeDuration > 0;

  return (
    <div>
      <PageTitle title="Tests" testid="dashboard-summary-title" />
      <TestRunMessages publicId={publicId} />
      <Grid container>
        {totalTestCount > 0 ? (
          <Grid
            item
            sm={3}
            xs={12}
            data-testid="dashboard-summary-test-count-section"
          >
            <TestCountList
              passedCount={totalPassingCount}
              failedCount={totalFailureCount}
              skippedCount={totalSkippedCount}
              totalCount={totalTestCount}
              horizontal={false}
            />
          </Grid>
        ) : null}
        {totalTestCount > 0 ? (
          <Hidden xsDown>
            <Grid
              item
              sm={3}
              xs={12}
              data-testid="dashboard-summary-duration-section"
            >
              {hasDurationData && (
                <TestRunDuration
                  publicId={publicId}
                  averageDuration={averageDuration}
                  cumulativeDuration={cumulativeDuration}
                  wallClockDuration={wallClockDuration}
                  slowestTestCaseDuration={slowestTestCaseDuration}
                />
              )}
            </Grid>
          </Hidden>
        ) : null}
        <Grid
          item
          sm={4}
          xs={12}
          data-testid="dashboard-summary-metadata-section"
        >
          <List dense={true}>
            {gitMetadata && gitMetadata.repoName && (
              <GitRepoListItem gitMetadata={gitMetadata} />
            )}
            {gitMetadata && gitMetadata.branchName && (
              <DashboardSummaryItem
                label="Branch"
                testId="dashboard-summary-branch-name"
                value={gitMetadata.branchName}
              />
            )}
            {gitMetadata && gitMetadata.projectName && (
              <DashboardSummaryItem
                label="Project"
                testId="dashboard-summary-project-name"
                value={gitMetadata.projectName}
              />
            )}
            <TestRunDate createdTimestamp={createdTimestamp} />

            <TestRunCleanupDate createdTimestamp={createdTimestamp} />
          </List>
        </Grid>
      </Grid>
    </div>
  );
};

export default DashboardSummary;
