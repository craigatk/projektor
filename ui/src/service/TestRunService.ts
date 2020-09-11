import { AxiosResponse } from "axios";
import {
  TestRun,
  TestCase,
  TestSuite,
  TestSuiteOutput,
  TestRunSummary,
  TestResultsProcessing,
  Attachments,
  TestRunSystemAttributes,
  Messages,
  CoverageStats,
  Coverage,
  CoverageExists,
  TestRunGitMetadata,
} from "../model/TestRunModel";
import TestSuiteOutputType from "./TestSuiteOutputType";
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
  outputType: TestSuiteOutputType
): Promise<AxiosResponse<TestSuiteOutput>> => {
  const action =
    outputType === TestSuiteOutputType.SystemOut ? "systemOut" : "systemErr";

  // @ts-ignore
  return axiosInstance.get<TestSuiteOutput>(
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
  axiosInstanceWithoutCache.get(`/run/${publicId}/coverage`);

const fetchCoverageExists = (
  publicId: string
): Promise<AxiosResponse<CoverageExists>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get(`/run/${publicId}/coverage/exists`);

const fetchOverallCoverageStats = (
  publicId: string
): Promise<AxiosResponse<CoverageStats>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get(`/run/${publicId}/coverage/overall`);

export {
  fetchAttachments,
  fetchMessages,
  fetchTestRun,
  fetchTestRunSummary,
  fetchTestRunGitMetadata,
  fetchFailedTestCases,
  fetchCoverage,
  fetchCoverageExists,
  fetchOverallCoverageStats,
  fetchSlowTestCases,
  fetchTestCaseDetails,
  fetchTestSuitesInPackage,
  fetchTestSuite,
  fetchTestSuiteSystemOutput,
  fetchTestResultsProcessing,
  fetchTestRunSystemAttributes,
  pinTestRun,
  unpinTestRun,
};
