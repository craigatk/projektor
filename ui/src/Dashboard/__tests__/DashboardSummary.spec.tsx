import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import mediaQuery from "css-mediaquery";
import { TestRunGitMetadata, TestRunSummary } from "../../model/TestRunModel";
import DashboardSummary from "../DashboardSummary";
import { PinState } from "../../Pin/PinState";

// From https://material-ui.com/components/use-media-query/#testing
function createMatchMedia(width) {
  return (query) => ({
    matches: mediaQuery.match(query, { width }),
    addEventListener: () => {},
    removeEventListener: () => {},
  });
}

jest.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

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

  beforeAll(() => {
    // @ts-ignore
    window.matchMedia = createMatchMedia(window.innerWidth);
  });

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
      </PinState>,
    );

    expect(getByTestId("dashboard-summary-project-name")).toHaveTextContent(
      "server",
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
      </PinState>,
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
      </PinState>,
    );

    expect(getByTestId("dashboard-summary-branch-name")).toHaveTextContent(
      "main",
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
      </PinState>,
    );

    expect(queryByTestId("dashboard-summary-branch-name")).toBeNull();
  });

  it("should display duration when it is set", () => {
    const { getByTestId } = render(
      <PinState publicId={publicId}>
        <DashboardSummary
          publicId={publicId}
          testRunSummary={testRunSummary}
          gitMetadata={null}
        />
      </PinState>,
    );

    expect(getByTestId("test-run-cumulative-duration")).toHaveTextContent(
      "10.000s",
    );
    expect(getByTestId("test-run-average-duration")).toHaveTextContent("2.5s");
  });

  it("should not display duration section when average duration is 0", () => {
    const testRunSummaryWithoutDuration = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: false,
      cumulativeDuration: 0,
      averageDuration: 0,
      slowestTestCaseDuration: 0,
    } as TestRunSummary;

    const { getByTestId } = render(
      <PinState publicId={publicId}>
        <DashboardSummary
          publicId={publicId}
          testRunSummary={testRunSummaryWithoutDuration}
          gitMetadata={null}
        />
      </PinState>,
    );

    expect(getByTestId("dashboard-summary-duration-section")).toBeEmpty();
  });
});
