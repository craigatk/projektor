import * as React from "react";
import { TestRunGitMetadata } from "../model/TestRunModel";
import { Link } from "@material-ui/core";
import { createGitHubUrl } from "./VersionControlHelpers";

interface GitHubFileLinkProps {
  gitMetadata: TestRunGitMetadata;
  filePath: string;
  lineNumber?: number;
  linkText: string;
  testId: string;
}

const GitHubFileLink = ({
  gitMetadata,
  filePath,
  lineNumber,
  linkText,
  testId,
}: GitHubFileLinkProps) => {
  const linkUrl = createGitHubUrl(gitMetadata, filePath, lineNumber);

  if (linkUrl) {
    return (
      <Link href={linkUrl} target="_blank" data-testid={testId}>
        {linkText}
      </Link>
    );
  } else {
    return <span data-testid={testId}>{linkText}</span>;
  }
};

export default GitHubFileLink;
