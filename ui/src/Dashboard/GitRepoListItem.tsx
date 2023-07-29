import * as React from "react";
import CleanLink from "../Link/CleanLink";
import { TestRunGitMetadata } from "../model/TestRunModel";
import DashboardSummaryItem from "./DashboardSummaryItem";
import { repositoryLinkUrlUI } from "../Repository/RepositoryLink";

interface GitRepoListItemProps {
  gitMetadata: TestRunGitMetadata;
}

const GitRepoListItem = ({ gitMetadata }: GitRepoListItemProps) => {
  const repoNameOnly = gitMetadata.repoName.split("/")[1];

  return (
    <DashboardSummaryItem
      label="Repository"
      testId="dashboard-git-repo"
      value={
        <span>
          <CleanLink
            to={`/organization/${gitMetadata.orgName}/`}
            data-testid="dashboard-summary-git-org-link"
          >
            {gitMetadata.orgName}
          </CleanLink>{" "}
          /{" "}
          <CleanLink
            to={repositoryLinkUrlUI(
              gitMetadata.repoName,
              gitMetadata.projectName,
              null,
            )}
            data-testid="dashboard-summary-git-repo-link"
          >
            {repoNameOnly}
          </CleanLink>
        </span>
      }
    />
  );
};

export default GitRepoListItem;
