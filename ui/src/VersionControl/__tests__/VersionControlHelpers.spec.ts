import { createGitHubUrl, createGitHubFileUrl } from "../VersionControlHelpers";
import { TestRunGitMetadata } from "../../model/TestRunModel";

describe("VersionControlHelpers", () => {
  describe("GitHub URL", () => {
    it("should create GitHub URL when URI does not start with slash", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com",
      } as TestRunGitMetadata;

      const uri = "pull/397";

      const url = createGitHubUrl(gitMetadata, uri);

      expect(url).toBe("https://github.com/craigatk/projektor/pull/397");
    });

    it("should create GitHub URL when URI does starts with slash", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com",
      } as TestRunGitMetadata;

      const uri = "/pull/397";

      const url = createGitHubUrl(gitMetadata, uri);

      expect(url).toBe("https://github.com/craigatk/projektor/pull/397");
    });
  });

  describe("GitHub file URL", () => {
    it("should create GitHub file URL when base URL does not end in slash", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com",
      } as TestRunGitMetadata;

      const filePath = "ui/package.json";

      const url = createGitHubFileUrl(gitMetadata, filePath);

      expect(url).toBe(
        "https://github.com/craigatk/projektor/blob/master/ui/package.json",
      );
    });

    it("should create GitHub file URL when base URL ends in slash", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com/",
      } as TestRunGitMetadata;

      const filePath = "ui/package.json";

      const url = createGitHubFileUrl(gitMetadata, filePath);

      expect(url).toBe(
        "https://github.com/craigatk/projektor/blob/master/ui/package.json",
      );
    });

    it("should create GitHub file URL when line number set", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com",
      } as TestRunGitMetadata;

      const filePath = "ui/package.json";
      const lineNumber = 2;

      const url = createGitHubFileUrl(gitMetadata, filePath, lineNumber);

      expect(url).toBe(
        "https://github.com/craigatk/projektor/blob/master/ui/package.json#L2",
      );
    });

    it("should return null for GitHub file URL when base URL not set", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: null,
      } as TestRunGitMetadata;

      const filePath = "ui/package.json";

      const url = createGitHubFileUrl(gitMetadata, filePath);

      expect(url).toBeNull();
    });

    it("should return null for GitHub file URL when no Git metadata", () => {
      const gitMetadata = null;

      const filePath = "ui/package.json";

      const url = createGitHubFileUrl(gitMetadata, filePath);

      expect(url).toBeNull();
    });

    it("should return null for GitHub file URL when file path not set", () => {
      const gitMetadata = {
        repoName: "craigatk/projektor",
        orgName: "craigatk",
        branchName: "master",
        gitHubBaseUrl: "https://github.com",
      } as TestRunGitMetadata;

      const filePath = null;

      const url = createGitHubFileUrl(gitMetadata, filePath);

      expect(url).toBeNull();
    });
  });
});
