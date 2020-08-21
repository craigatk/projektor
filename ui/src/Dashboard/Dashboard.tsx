import * as React from "react";
import FailedTestCases from "../TestCase/FailedTestCases";
import { TestRunSummary } from "../model/TestRunModel";
import DashboardSummary from "./DashboardSummary";
import { RouteComponentProps } from "@reach/router";
import TestRunAllTests from "../TestRun/TestRunAllTests";
import CoverageSummary from "../Coverage/CoverageSummary";

interface DashboardProps extends RouteComponentProps {
  publicId: string;
  testRunSummary: TestRunSummary;
}

const Dashboard = ({ publicId, testRunSummary }: DashboardProps) => {
  return (
    <div>
      <DashboardSummary publicId={publicId} testRunSummary={testRunSummary} />
      <CoverageSummary publicId={publicId} />
      {testRunSummary.passed ? (
        <TestRunAllTests publicId={publicId} />
      ) : (
        <FailedTestCases publicId={publicId} />
      )}
    </div>
  );
};

export default Dashboard;
