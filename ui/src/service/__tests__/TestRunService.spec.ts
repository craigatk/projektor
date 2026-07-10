import MockAdapter from "axios-mock-adapter";
import { axiosInstance, axiosInstanceWithoutCache } from "../AxiosService";
import TestOutputType from "../TestOutputType";
import {
  fetchAttachments,
  fetchCodeQualityReports,
  fetchMessages,
  fetchTestRun,
  fetchTestRunSummary,
  fetchTestRunGitMetadata,
  fetchFailedTestCases,
  fetchCoverage,
  fetchCoverageExists,
  fetchCoverageGroupFiles,
  fetchCoverageBadge,
  fetchTestsBadge,
  fetchPerformanceResults,
  fetchOverallCoverageStats,
  fetchSlowTestCases,
  fetchTestCaseDetails,
  fetchTestCaseFailureAnalysis,
  fetchTestCaseSystemOutput,
  fetchTestSuitesInPackage,
  fetchTestSuite,
  fetchTestSuiteSystemOutput,
  fetchTestResultsProcessing,
  fetchTestRunSystemAttributes,
  pinTestRun,
  unpinTestRun,
} from "../TestRunService";

vi.mock("../EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

const publicId = "TESTRUN1";

describe("TestRunService", () => {
  let mockAxios;
  let mockAxiosWithoutCache;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
    // @ts-ignore
    mockAxiosWithoutCache = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
    mockAxiosWithoutCache.restore();
  });

  it("should fetch the test run", async () => {
    const testRun = { publicId };
    mockAxios.onGet(`http://localhost:8080/run/${publicId}`).reply(200, testRun);

    const response = await fetchTestRun(publicId);

    expect(response.data).toEqual(testRun);
  });

  it("should fetch the test run summary", async () => {
    const summary = { totalCount: 10 };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/summary`)
      .reply(200, summary);

    const response = await fetchTestRunSummary(publicId);

    expect(response.data).toEqual(summary);
  });

  it("should fetch the test run git metadata", async () => {
    const gitMetadata = { repoName: "my-repo" };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/metadata/git`)
      .reply(200, gitMetadata);

    const response = await fetchTestRunGitMetadata(publicId);

    expect(response.data).toEqual(gitMetadata);
  });

  it("should fetch failed test cases", async () => {
    const failedCases = [{ name: "test1" }];
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/failed`)
      .reply(200, failedCases);

    const response = await fetchFailedTestCases(publicId);

    expect(response.data).toEqual(failedCases);
  });

  it("should fetch slow test cases", async () => {
    const slowCases = [{ name: "test1" }];
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/cases/slow`)
      .reply(200, slowCases);

    const response = await fetchSlowTestCases(publicId);

    expect(response.data).toEqual(slowCases);
  });

  it("should fetch test case details", async () => {
    const testCase = { name: "test1" };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suite/1/case/2`)
      .reply(200, testCase);

    const response = await fetchTestCaseDetails(publicId, 1, 2);

    expect(response.data).toEqual(testCase);
  });

  describe("fetchTestCaseSystemOutput", () => {
    it("should fetch system out when output type is SystemOut", async () => {
      const output = { output: "some stdout" };
      mockAxios
        .onGet(`http://localhost:8080/run/${publicId}/suite/1/case/2/systemOut`)
        .reply(200, output);

      const response = await fetchTestCaseSystemOutput(
        publicId,
        1,
        2,
        TestOutputType.SystemOut,
      );

      expect(response.data).toEqual(output);
    });

    it("should fetch system err when output type is SystemErr", async () => {
      const output = { output: "some stderr" };
      mockAxios
        .onGet(`http://localhost:8080/run/${publicId}/suite/1/case/2/systemErr`)
        .reply(200, output);

      const response = await fetchTestCaseSystemOutput(
        publicId,
        1,
        2,
        TestOutputType.SystemErr,
      );

      expect(response.data).toEqual(output);
    });
  });

  it("should fetch test case failure analysis", async () => {
    const analysis = { summary: "flaky" };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suite/1/case/2/analysis`)
      .reply(200, analysis);

    const response = await fetchTestCaseFailureAnalysis(publicId, 1, 2);

    expect(response.data).toEqual(analysis);
  });

  it("should fetch test suites in a package", async () => {
    const suites = [{ name: "suite1" }];
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suites?package=com.example`)
      .reply(200, suites);

    const response = await fetchTestSuitesInPackage(publicId, "com.example");

    expect(response.data).toEqual(suites);
  });

  it("should fetch a test suite", async () => {
    const suite = { name: "suite1" };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/suite/1`)
      .reply(200, suite);

    const response = await fetchTestSuite(publicId, 1);

    expect(response.data).toEqual(suite);
  });

  describe("fetchTestSuiteSystemOutput", () => {
    it("should fetch system out when output type is SystemOut", async () => {
      const output = { output: "some stdout" };
      mockAxios
        .onGet(`http://localhost:8080/run/${publicId}/suite/1/systemOut`)
        .reply(200, output);

      const response = await fetchTestSuiteSystemOutput(
        publicId,
        1,
        TestOutputType.SystemOut,
      );

      expect(response.data).toEqual(output);
    });

    it("should fetch system err when output type is SystemErr", async () => {
      const output = { output: "some stderr" };
      mockAxios
        .onGet(`http://localhost:8080/run/${publicId}/suite/1/systemErr`)
        .reply(200, output);

      const response = await fetchTestSuiteSystemOutput(
        publicId,
        1,
        TestOutputType.SystemErr,
      );

      expect(response.data).toEqual(output);
    });
  });

  it("should fetch test results processing status", async () => {
    const processing = { complete: true };
    mockAxiosWithoutCache
      .onGet(`http://localhost:8080/results/${publicId}/status`)
      .reply(200, processing);

    const response = await fetchTestResultsProcessing(publicId);

    expect(response.data).toEqual(processing);
  });

  it("should fetch attachments", async () => {
    const attachments = { attachments: [] };
    mockAxiosWithoutCache
      .onGet(`http://localhost:8080/run/${publicId}/attachments`)
      .reply(200, attachments);

    const response = await fetchAttachments(publicId);

    expect(response.data).toEqual(attachments);
  });

  it("should fetch test run system attributes", async () => {
    const attributes = { pinned: true };
    mockAxiosWithoutCache
      .onGet(`http://localhost:8080/run/${publicId}/attributes`)
      .reply(200, attributes);

    const response = await fetchTestRunSystemAttributes(publicId);

    expect(response.data).toEqual(attributes);
  });

  it("should pin a test run", async () => {
    mockAxiosWithoutCache
      .onPost(`http://localhost:8080/run/${publicId}/attributes/pin`)
      .reply(200);

    const response = await pinTestRun(publicId);

    expect(response.status).toBe(200);
  });

  it("should unpin a test run", async () => {
    mockAxiosWithoutCache
      .onPost(`http://localhost:8080/run/${publicId}/attributes/unpin`)
      .reply(200);

    const response = await unpinTestRun(publicId);

    expect(response.status).toBe(200);
  });

  it("should fetch messages", async () => {
    const messages = { messages: ["hello"] };
    mockAxiosWithoutCache
      .onGet(`http://localhost:8080/run/${publicId}/messages`)
      .reply(200, messages);

    const response = await fetchMessages(publicId);

    expect(response.data).toEqual(messages);
  });

  it("should fetch coverage", async () => {
    const coverage = { overall: {} };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/coverage`)
      .reply(200, coverage);

    const response = await fetchCoverage(publicId);

    expect(response.data).toEqual(coverage);
  });

  it("should fetch whether coverage exists", async () => {
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/coverage/exists`)
      .reply(200, { exists: true });

    const response = await fetchCoverageExists(publicId);

    expect(response.data).toEqual({ exists: true });
  });

  it("should fetch overall coverage stats", async () => {
    const stats = { lineTotalCount: 100 };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/coverage/overall`)
      .reply(200, stats);

    const response = await fetchOverallCoverageStats(publicId);

    expect(response.data).toEqual(stats);
  });

  it("should fetch coverage group files", async () => {
    const files = { files: [] };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/coverage/my-group/files`)
      .reply(200, files);

    const response = await fetchCoverageGroupFiles(publicId, "my-group");

    expect(response.data).toEqual(files);
  });

  it("should fetch the coverage badge", async () => {
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/badge/coverage`)
      .reply(200, "<svg></svg>");

    const response = await fetchCoverageBadge(publicId);

    expect(response.data).toBe("<svg></svg>");
  });

  it("should fetch the tests badge", async () => {
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/badge/tests`)
      .reply(200, "<svg></svg>");

    const response = await fetchTestsBadge(publicId);

    expect(response.data).toBe("<svg></svg>");
  });

  it("should fetch performance results", async () => {
    const results = { results: [] };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/performance`)
      .reply(200, results);

    const response = await fetchPerformanceResults(publicId);

    expect(response.data).toEqual(results);
  });

  it("should fetch code quality reports", async () => {
    const reports = { reports: [] };
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/quality`)
      .reply(200, reports);

    const response = await fetchCodeQualityReports(publicId);

    expect(response.data).toEqual(reports);
  });
});
