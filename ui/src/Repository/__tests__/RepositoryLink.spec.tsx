import { repositoryLinkUrlAPI, repositoryLinkUrlUI } from "../RepositoryLink";

describe("RepositoryLink", () => {
  describe("repositoryLinkUrlUI", () => {
    const repoName = "my-org/my-repo";
    const projectName = "my-project";
    const uri = "/coverage";

    it("when repo name only should create link", () => {
      const linkUrl = repositoryLinkUrlUI(repoName, null, null);
      expect(linkUrl).toEqual("/repository/my-org/my-repo");
    });

    it("when repo name and uri should create link", () => {
      const linkUrl = repositoryLinkUrlUI(repoName, null, uri);
      expect(linkUrl).toEqual("/repository/my-org/my-repo/coverage");
    });

    it("when uri doesn't start with forward slash should add it", () => {
      const linkUrl = repositoryLinkUrlUI(repoName, null, "coverage");
      expect(linkUrl).toEqual("/repository/my-org/my-repo/coverage");
    });

    it("when repo name and project name should create link", () => {
      const linkUrl = repositoryLinkUrlUI(repoName, projectName, null);
      expect(linkUrl).toEqual("/repository/my-org/my-repo/project/my-project");
    });

    it("when repo name, project name, and uri should create link", () => {
      const linkUrl = repositoryLinkUrlUI(repoName, projectName, uri);
      expect(linkUrl).toEqual(
        "/repository/my-org/my-repo/project/my-project/coverage"
      );
    });
  });

  describe("repositoryLinkUrlAPI", () => {
    const repoName = "my-org/my-repo";
    const projectName = "my-project";
    const uri = "/coverage";

    it("when repo name only should create link", () => {
      const linkUrl = repositoryLinkUrlAPI(repoName, null, null);
      expect(linkUrl).toEqual("/repo/my-org/my-repo");
    });

    it("when repo name and uri should create link", () => {
      const linkUrl = repositoryLinkUrlAPI(repoName, null, uri);
      expect(linkUrl).toEqual("/repo/my-org/my-repo/coverage");
    });

    it("when uri doesn't start with forward slash should add it", () => {
      const linkUrl = repositoryLinkUrlAPI(repoName, null, "coverage");
      expect(linkUrl).toEqual("/repo/my-org/my-repo/coverage");
    });

    it("when repo name and project name should create link", () => {
      const linkUrl = repositoryLinkUrlAPI(repoName, projectName, null);
      expect(linkUrl).toEqual("/repo/my-org/my-repo/project/my-project");
    });

    it("when repo name, project name, and uri should create link", () => {
      const linkUrl = repositoryLinkUrlAPI(repoName, projectName, uri);
      expect(linkUrl).toEqual(
        "/repo/my-org/my-repo/project/my-project/coverage"
      );
    });
  });
});
