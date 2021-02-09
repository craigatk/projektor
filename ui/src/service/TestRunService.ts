import { AxiosResponse } from "axios";
import {
  TestRun,
  TestCase,
  TestSuite,
  TestOutput,
  TestRunSummary,
  TestResultsProcessing,
  Attachments,
  TestRunSystemAttributes,
  Messages,
  CoverageStats,
  Coverage,
  CoverageExists,
  TestRunGitMetadata,
  CoverageFiles,
  PerformanceResults,
} from "../model/TestRunModel";
import TestOutputType from "./TestOutputType";
import { axiosInstance, axiosInstanceWithoutCache } from "./AxiosService";

const fetchTestRun = (publicId: string): Promise<AxiosResponse<TestRun>> => {
  // @ts-ignore
  return axiosInstance.get<TestRun>(`run/${publicId}`);
};

const fetchTestRunSummary = (
  publicId: string
): Promise<AxiosResponse<TestRunSummary>> =>
  // @ts-ignore
  axiosInstance.get<TestRunSummary>(`run/${publicId}/summary`);

const fetchTestRunGitMetadata = (
  publicId: string
): Promise<AxiosResponse<TestRunGitMetadata>> =>
  // @ts-ignore
  axiosInstance.get<TestRunGitMetadata>(`run/${publicId}/metadata/git`);

const fetchFailedTestCases = (
  publicId: String
): Promise<AxiosResponse<TestCase[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase[]>(`run/${publicId}/cases/failed`);
};

const fetchSlowTestCases = (
  publicId: string
): Promise<AxiosResponse<TestCase[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase[]>(`run/${publicId}/cases/slow`);
};

const fetchTestCaseDetails = (
  publicId: string,
  testSuiteIdx: number,
  testCaseIdx: number
): Promise<AxiosResponse<TestCase>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase>(
    `/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
  );
};

const fetchTestCaseSystemOutput = (
  publicId: string,
  testSuiteIdx: number,
  testCaseIdx: number,
  outputType: TestOutputType
): Promise<AxiosResponse<TestOutput>> => {
  const action =
    outputType === TestOutputType.SystemOut ? "systemOut" : "systemErr";

  // @ts-ignore
  return axiosInstance.get<TestOutput>(
    `/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}/${action}`
  );
};

const fetchTestSuitesInPackage = (
  publicId: string,
  packageName: string
): Promise<AxiosResponse<TestSuite[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestSuite[]>(
    `/run/${publicId}/suites?package=${packageName}`
  );
};

const fetchTestSuite = (
  publicId: string,
  testSuiteIdx: number
): Promise<AxiosResponse<TestSuite>> =>
  // @ts-ignore
  axiosInstance.get<TestSuite>(`/run/${publicId}/suite/${testSuiteIdx}`);

const fetchTestSuiteSystemOutput = (
  publicId: string,
  testSuiteIdx: number,
  outputType: TestOutputType
): Promise<AxiosResponse<TestOutput>> => {
  const action =
    outputType === TestOutputType.SystemOut ? "systemOut" : "systemErr";

  // @ts-ignore
  return axiosInstance.get<TestOutput>(
    `/run/${publicId}/suite/${testSuiteIdx}/${action}`
  );
};

const fetchTestResultsProcessing = (
  publicId: string
): Promise<AxiosResponse<TestResultsProcessing>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<TestResultsProcessing>(
    `/results/${publicId}/status`
  );

const fetchAttachments = (
  publicId: string
): Promise<AxiosResponse<Attachments>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<Attachments>(`/run/${publicId}/attachments`);

const fetchTestRunSystemAttributes = (
  publicId: string
): Promise<AxiosResponse<TestRunSystemAttributes>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<TestRunSystemAttributes>(
    `/run/${publicId}/attributes`
  );

const pinTestRun = (publicId: string): Promise<AxiosResponse<void>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.post(`/run/${publicId}/attributes/pin`);

const unpinTestRun = (publicId: string): Promise<AxiosResponse<void>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.post(`/run/${publicId}/attributes/unpin`);

const fetchMessages = (publicId: string): Promise<AxiosResponse<Messages>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get(`/run/${publicId}/messages`);

const fetchCoverage = (publicId: string): Promise<AxiosResponse<Coverage>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/coverage`);

const fetchCoverageExists = (
  publicId: string
): Promise<AxiosResponse<CoverageExists>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/coverage/exists`);

const fetchOverallCoverageStats = (
  publicId: string
): Promise<AxiosResponse<CoverageStats>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/coverage/overall`);

const fetchCoverageGroupFiles = (
  publicId: string,
  coverageGroupName: string
): Promise<AxiosResponse<CoverageFiles>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/coverage/${coverageGroupName}/files`);

const fetchCoverageBadge = (publicId: string): Promise<AxiosResponse<string>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/badge/coverage`);

const fetchPerformanceResults = (
  publicId: string
): Promise<AxiosResponse<PerformanceResults>> =>
  // @ts-ignore
  axiosInstance.get(`/run/${publicId}/performance`);

export {
  fetchAttachments,
  fetchMessages,
  fetchTestRun,
  fetchTestRunSummary,
  fetchTestRunGitMetadata,
  fetchFailedTestCases,
  fetchCoverage,
  fetchCoverageExists,
  fetchCoverageGroupFiles,
  fetchCoverageBadge,
  fetchPerformanceResults,
  fetchOverallCoverageStats,
  fetchSlowTestCases,
  fetchTestCaseDetails,
  fetchTestCaseSystemOutput,
  fetchTestSuitesInPackage,
  fetchTestSuite,
  fetchTestSuiteSystemOutput,
  fetchTestResultsProcessing,
  fetchTestRunSystemAttributes,
  pinTestRun,
  unpinTestRun,
};
