import { createGitHubUrl } from "../VersionControlHelpers";
import { TestRunGitMetadata } from "../../model/TestRunModel";

describe("VersionControlHelpers", () => {
  it("should create GitHub URL when base URL does not end in slash", () => {
    const gitMetadata = {
      repoName: "craigatk/projektor",
      orgName: "craigatk",
      branchName: "master",
      gitHubBaseUrl: "https://github.com",
    } as TestRunGitMetadata;

    const filePath = "ui/package.json";

    const url = createGitHubUrl(gitMetadata, filePath);

    expect(url).toBe(
      "https://github.com/craigatk/projektor/blob/master/ui/package.json"
    );
  });

  it("should create GitHub URL when base URL ends in slash", () => {
    const gitMetadata = {
      repoName: "craigatk/projektor",
      orgName: "craigatk",
      branchName: "master",
      gitHubBaseUrl: "https://github.com/",
    } as TestRunGitMetadata;

    const filePath = "ui/package.json";

    const url = createGitHubUrl(gitMetadata, filePath);

    expect(url).toBe(
      "https://github.com/craigatk/projektor/blob/master/ui/package.json"
    );
  });

  it("should create GitHub URL when line number set", () => {
    const gitMetadata = {
      repoName: "craigatk/projektor",
      orgName: "craigatk",
      branchName: "master",
      gitHubBaseUrl: "https://github.com",
    } as TestRunGitMetadata;

    const filePath = "ui/package.json";
    const lineNumber = 2;

    const url = createGitHubUrl(gitMetadata, filePath, lineNumber);

    expect(url).toBe(
      "https://github.com/craigatk/projektor/blob/master/ui/package.json#L2"
    );
  });

  it("should return null for GitHub URL when base URL not set", () => {
    const gitMetadata = {
      repoName: "craigatk/projektor",
      orgName: "craigatk",
      branchName: "master",
      gitHubBaseUrl: null,
    } as TestRunGitMetadata;

    const filePath = "ui/package.json";

    const url = createGitHubUrl(gitMetadata, filePath);

    expect(url).toBeNull();
  });

  it("should return null for GitHub URL when no Git metadata", () => {
    const gitMetadata = null;

    const filePath = "ui/package.json";

    const url = createGitHubUrl(gitMetadata, filePath);

    expect(url).toBeNull();
  });

  it("should return null for GitHub URL when file path not set", () => {
    const gitMetadata = {
      repoName: "craigatk/projektor",
      orgName: "craigatk",
      branchName: "master",
      gitHubBaseUrl: "https://github.com",
    } as TestRunGitMetadata;

    const filePath = null;

    const url = createGitHubUrl(gitMetadata, filePath);

    expect(url).toBeNull();
  });
});
