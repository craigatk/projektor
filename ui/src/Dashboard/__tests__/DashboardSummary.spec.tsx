import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import { TestRunGitMetadata, TestRunSummary } from "../../model/TestRunModel";
import DashboardSummary from "../DashboardSummary";
import { PinState } from "../../Pin/PinState";

describe("Dashboard summary", () => {
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

  it("should display project name when it exists", () => {
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

  it("should not display project name when it does not exists", () => {
    const gitMetadata = {
      repoName: "projektor/projektor",
      orgName: "projektor",
      projectName: null,
      branchName: "main",
      isMainBranch: true,
    } as TestRunGitMetadata;

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <DashboardSummary
          publicId={publicId}
          testRunSummary={testRunSummary}
          gitMetadata={gitMetadata}
        />
      </PinState>
    );

    expect(queryByTestId("dashboard-summary-project-name")).toBeNull();
  });

  it("should display branch name when it exists", () => {
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

    expect(getByTestId("dashboard-summary-branch-name")).toHaveTextContent(
      "main"
    );
  });

  it("should not display branch name when it does not exists", () => {
    const gitMetadata = {
      repoName: "projektor/projektor",
      orgName: "projektor",
      projectName: "server",
      branchName: null,
      isMainBranch: false,
    } as TestRunGitMetadata;

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <DashboardSummary
          publicId={publicId}
          testRunSummary={testRunSummary}
          gitMetadata={gitMetadata}
        />
      </PinState>
    );

    expect(queryByTestId("dashboard-summary-branch-name")).toBeNull();
  });
});
