import _ from "lodash";
import { TestRunGitMetadata } from "../model/TestRunModel";

const createGitHubUrl = (
  gitMetadata: TestRunGitMetadata,
  filePath: string,
  lineNumber?: number
): string => {
  if (gitMetadata && gitMetadata.gitHubBaseUrl && filePath) {
    const baseUrl = gitMetadata.gitHubBaseUrl.endsWith("/")
      ? gitMetadata.gitHubBaseUrl
      : `${gitMetadata.gitHubBaseUrl}/`;

    const url = `${baseUrl}${gitMetadata.repoName}/blob/${gitMetadata.branchName}/${filePath}`;

    return _.isNil(lineNumber) ? url : `${url}#L${lineNumber}`;
  } else {
    return null;
  }
};

export { createGitHubUrl };
