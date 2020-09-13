import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestRunGitMetadata, TestRunSummary } from "../../model/TestRunModel";
import DashboardSummary from "../DashboardSummary";
import { PinState } from "../../Pin/PinState";

describe("Dashboard summary", () => {
  it("should display project name when it exists", () => {
    const publicId = "34567";

    const testRunSummary = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: false,
      cumulativeDuration: 10.0,
      averageDuration: 2.5,
      slowestTestCaseDuration: 5.0,
    } as TestRunSummary;

    const gitMetadata = {
      repoName: "projektor/projektor",
      orgName: "projektor",
      projectName: "server",
      branchName: "main",
      isMainBranch: true,
    } as TestRunGitMetadata;

    const { getByTestId } = render(
      <PinState publicId={publicId}>
        <DashboardSummary
          publicId={publicId}
          testRunSummary={testRunSummary}
          gitMetadata={gitMetadata}
        />
      </PinState>
    );

    expect(getByTestId("dashboard-summary-project-name")).toHaveTextContent(
      "server"
    );
  });
});
