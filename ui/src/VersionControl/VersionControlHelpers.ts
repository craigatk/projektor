import _ from "lodash";
import { TestRunGitMetadata } from "../model/TestRunModel";

const createGitHubUrl = (
  gitMetadata: TestRunGitMetadata,
  uri: string
): string | null => {
  if (gitMetadata && gitMetadata.gitHubBaseUrl && uri) {
    const baseUrl = gitMetadata.gitHubBaseUrl.endsWith("/")
      ? gitMetadata.gitHubBaseUrl
      : `${gitMetadata.gitHubBaseUrl}/`;

    const theUri = uri.startsWith("/") ? uri : `/${uri}`;

    return `${baseUrl}${gitMetadata.repoName}${theUri}`;
  } else {
    return null;
  }
};

const createGitHubFileUrl = (
  gitMetadata: TestRunGitMetadata,
  filePath: string,
  lineNumber?: number
): string | null => {
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

export { createGitHubUrl, createGitHubFileUrl };
