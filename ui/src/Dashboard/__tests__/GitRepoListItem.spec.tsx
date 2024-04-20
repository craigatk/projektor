import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import { TestRunGitMetadata } from "../../model/TestRunModel";
import GitRepoListItem from "../GitRepoListItem";

describe("GitRepoListItem", () => {
  it("should display org link", () => {
    const gitMetadata = {
      orgName: "my-org",
      repoName: "my-org/my-cov",
      branchName: "main",
      isMainBranch: true,
    } as TestRunGitMetadata;

    const { getByTestId } = render(
      <GitRepoListItem gitMetadata={gitMetadata} />,
    );

    expect(getByTestId("dashboard-summary-git-org-link")).toHaveTextContent(
      "my-org",
    );
  });

  it("should display repo link", () => {
    const gitMetadata = {
      orgName: "my-org",
      repoName: "my-org/my-cov",
      branchName: "main",
      isMainBranch: true,
    } as TestRunGitMetadata;

    const { getByTestId } = render(
      <GitRepoListItem gitMetadata={gitMetadata} />,
    );

    expect(getByTestId("dashboard-summary-git-repo-link")).toHaveTextContent(
      "my-cov",
    );
  });
});
