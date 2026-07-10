import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../AxiosService";
import {
  fetchRepositoryTimeline,
  fetchRepositoryCoverageTimeline,
  fetchRepositoryCoverageExists,
  fetchRepositoryCoverageBadge,
  fetchRepositoryTestsBadge,
  fetchRepositoryFlakyTests,
  fetchRepositoryPerformanceTimeline,
} from "../RepositoryService";

vi.mock("../EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("RepositoryService", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  describe("fetchRepositoryTimeline", () => {
    it("should fetch the repository timeline when no project is given", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/timeline")
        .reply(200, timeline);

      const response = await fetchRepositoryTimeline("my-repo");

      expect(response.data).toEqual(timeline);
    });

    it("should fetch the repository timeline for a specific project", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/project/my-project/timeline")
        .reply(200, timeline);

      const response = await fetchRepositoryTimeline("my-repo", "my-project");

      expect(response.data).toEqual(timeline);
    });
  });

  describe("fetchRepositoryCoverageTimeline", () => {
    it("should fetch the repository coverage timeline when no project or branch is given", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/coverage/timeline")
        .reply(200, timeline);

      const response = await fetchRepositoryCoverageTimeline("my-repo");

      expect(response.data).toEqual(timeline);
    });

    it("should fetch the repository coverage timeline for a specific project", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/coverage/timeline",
        )
        .reply(200, timeline);

      const response = await fetchRepositoryCoverageTimeline(
        "my-repo",
        "my-project",
      );

      expect(response.data).toEqual(timeline);
    });

    it("should uppercase the branch type query param when given", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/coverage/timeline?branch=MAIN",
        )
        .reply(200, timeline);

      const response = await fetchRepositoryCoverageTimeline(
        "my-repo",
        undefined,
        "main",
      );

      expect(response.data).toEqual(timeline);
    });
  });

  describe("fetchRepositoryCoverageExists", () => {
    it("should fetch whether coverage exists when no project is given", async () => {
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/coverage/exists")
        .reply(200, { exists: true });

      const response = await fetchRepositoryCoverageExists("my-repo");

      expect(response.data).toEqual({ exists: true });
    });

    it("should fetch whether coverage exists for a specific project", async () => {
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/coverage/exists",
        )
        .reply(200, { exists: false });

      const response = await fetchRepositoryCoverageExists(
        "my-repo",
        "my-project",
      );

      expect(response.data).toEqual({ exists: false });
    });
  });

  describe("fetchRepositoryCoverageBadge", () => {
    it("should fetch the coverage badge when no project is given", async () => {
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/badge/coverage")
        .reply(200, "<svg></svg>");

      const response = await fetchRepositoryCoverageBadge("my-repo");

      expect(response.data).toBe("<svg></svg>");
    });

    it("should fetch the coverage badge for a specific project", async () => {
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/badge/coverage",
        )
        .reply(200, "<svg></svg>");

      const response = await fetchRepositoryCoverageBadge(
        "my-repo",
        "my-project",
      );

      expect(response.data).toBe("<svg></svg>");
    });
  });

  describe("fetchRepositoryTestsBadge", () => {
    it("should fetch the tests badge when no project is given", async () => {
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/badge/tests")
        .reply(200, "<svg></svg>");

      const response = await fetchRepositoryTestsBadge("my-repo");

      expect(response.data).toBe("<svg></svg>");
    });

    it("should fetch the tests badge for a specific project", async () => {
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/badge/tests",
        )
        .reply(200, "<svg></svg>");

      const response = await fetchRepositoryTestsBadge(
        "my-repo",
        "my-project",
      );

      expect(response.data).toBe("<svg></svg>");
    });
  });

  describe("fetchRepositoryFlakyTests", () => {
    it("should fetch flaky tests when no project is given, uppercasing the branch type", async () => {
      const flakyTests = { flakyTests: [] };
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/tests/flaky?threshold=3&max_runs=10&branch_type=MAIN",
        )
        .reply(200, flakyTests);

      const response = await fetchRepositoryFlakyTests(
        10,
        3,
        "main",
        "my-repo",
      );

      expect(response.data).toEqual(flakyTests);
    });

    it("should fetch flaky tests for a specific project", async () => {
      const flakyTests = { flakyTests: [] };
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/tests/flaky?threshold=3&max_runs=10&branch_type=MAIN",
        )
        .reply(200, flakyTests);

      const response = await fetchRepositoryFlakyTests(
        10,
        3,
        "main",
        "my-repo",
        "my-project",
      );

      expect(response.data).toEqual(flakyTests);
    });
  });

  describe("fetchRepositoryPerformanceTimeline", () => {
    it("should fetch the performance timeline when no project is given", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet("http://localhost:8080/repo/my-repo/performance/timeline")
        .reply(200, timeline);

      const response = await fetchRepositoryPerformanceTimeline("my-repo");

      expect(response.data).toEqual(timeline);
    });

    it("should fetch the performance timeline for a specific project", async () => {
      const timeline = { timelineEntries: [] };
      mockAxios
        .onGet(
          "http://localhost:8080/repo/my-repo/project/my-project/performance/timeline",
        )
        .reply(200, timeline);

      const response = await fetchRepositoryPerformanceTimeline(
        "my-repo",
        "my-project",
      );

      expect(response.data).toEqual(timeline);
    });
  });
});
