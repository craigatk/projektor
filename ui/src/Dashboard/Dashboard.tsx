import * as React from "react";
import FailedTestCases from "../TestCase/FailedTestCases";
import { TestRunGitMetadata, TestRunSummary } from "../model/TestRunModel";
import DashboardSummary from "./DashboardSummary";
import { RouteComponentProps } from "@reach/router";
import TestRunAllTests from "../TestRun/TestRunAllTests";
import CoverageSummary from "../Coverage/CoverageSummary";
import PerformanceSection from "../Performance/PerformanceSection";

interface DashboardProps extends RouteComponentProps {
  publicId: string;
  testRunSummary: TestRunSummary;
  gitMetadata?: TestRunGitMetadata;
}

const Dashboard = ({
  publicId,
  testRunSummary,
  gitMetadata,
}: DashboardProps) => {
  return (
    <div>
      <DashboardSummary
        publicId={publicId}
        testRunSummary={testRunSummary}
        gitMetadata={gitMetadata}
      />
      <CoverageSummary publicId={publicId} gitMetadata={gitMetadata} />
      {testRunSummary.passed ? (
        testRunSummary.totalTestCount > 0 ? (
          <TestRunAllTests publicId={publicId} />
        ) : null
      ) : (
        <FailedTestCases publicId={publicId} />
      )}
      <PerformanceSection publicId={publicId} />
    </div>
  );
};

export default Dashboard;
