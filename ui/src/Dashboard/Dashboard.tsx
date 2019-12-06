import * as React from "react";
import FailedTestCases from "../TestCase/FailedTestCases";
import { TestRunSummary } from "../model/TestRunModel";
import DashboardSummary from "./DashboardSummary";
import { RouteComponentProps } from "@reach/router";
import TestRunAllTests from "../TestRun/TestRunAllTests";

interface DashboardProps extends RouteComponentProps {
  publicId: string;
  testRunSummary: TestRunSummary;
}

const Dashboard = ({ publicId, testRunSummary }: DashboardProps) => {
  return (
    <div>
      <DashboardSummary publicId={publicId} testRunSummary={testRunSummary} />
      {testRunSummary.passed ? (
        <TestRunAllTests publicId={publicId} />
      ) : (
        <FailedTestCases publicId={publicId} />
      )}
    </div>
  );
};

export default Dashboard;
