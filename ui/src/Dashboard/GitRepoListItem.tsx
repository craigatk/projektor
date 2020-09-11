import * as React from "react";
import CleanLink from "../Link/CleanLink";
import { TestRunGitMetadata } from "../model/TestRunModel";
import DashboardSummaryItem from "./DashboardSummaryItem";

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
          <CleanLink to={`/organization/${gitMetadata.orgName}/`}>
            {gitMetadata.orgName}
          </CleanLink>
          /{repoNameOnly}
        </span>
      }
    />
  );
};

export default GitRepoListItem;
