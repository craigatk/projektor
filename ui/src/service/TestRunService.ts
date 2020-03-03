import axios, { AxiosResponse } from "axios";
import applyCaseConverters from "axios-case-converter";
import { setupCache } from "axios-cache-adapter";
import {
  TestRun,
  TestCase,
  TestSuite,
  TestSuiteOutput,
  TestRunSummary,
  TestResultsProcessing,
  Attachments
} from "../model/TestRunModel";
import TestSuiteOutputType from "./TestSuiteOutputType";

const cache = setupCache({
  maxAge: 15 * 60 * 1000
});

const myAxios = axios.create({
  baseURL: process.env.API_BASE_URL,
  adapter: cache.adapter
});
// @ts-ignore
const axiosInstance = applyCaseConverters(myAxios);

const axiosInstanceWithoutCache = applyCaseConverters(
  // @ts-ignore
  axios.create({
    baseURL: process.env.API_BASE_URL
  })
);

const fetchTestRun = (publicId: String): Promise<AxiosResponse<TestRun>> => {
  // @ts-ignore
  return axiosInstance.get<TestRun>(`run/${publicId}`);
};

const fetchTestRunSummary = (
  publicId: String
): Promise<AxiosResponse<TestRunSummary>> =>
  // @ts-ignore
  axiosInstance.get<TestRunSummary>(`run/${publicId}/summary`);

const fetchFailedTestCases = (
  publicId: String
): Promise<AxiosResponse<TestCase[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase[]>(`run/${publicId}/cases/failed`);
};

const fetchSlowTestCases = (
  publicId: String
): Promise<AxiosResponse<TestCase[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase[]>(`run/${publicId}/cases/slow`);
};

const fetchTestCaseDetails = (
  publicId: String,
  testSuiteIdx: number,
  testCaseIdx: number
): Promise<AxiosResponse<TestCase>> => {
  // @ts-ignore
  return axiosInstance.get<TestCase>(
    `/run/${publicId}/suite/${testSuiteIdx}/case/${testCaseIdx}`
  );
};

const fetchTestSuitesInPackage = (
  publicId: String,
  packageName: String
): Promise<AxiosResponse<TestSuite[]>> => {
  // @ts-ignore
  return axiosInstance.get<TestSuite[]>(
    `/run/${publicId}/suites?package=${packageName}`
  );
};

const fetchTestSuite = (
  publicId: String,
  testSuiteIdx: number
): Promise<AxiosResponse<TestSuite>> =>
  // @ts-ignore
  axiosInstance.get<TestSuite>(`/run/${publicId}/suite/${testSuiteIdx}`);

const fetchTestSuiteSystemOutput = (
  publicId: String,
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

export {
  fetchAttachments,
  fetchTestRun,
  fetchTestRunSummary,
  fetchFailedTestCases,
  fetchSlowTestCases,
  fetchTestCaseDetails,
  fetchTestSuitesInPackage,
  fetchTestSuite,
  fetchTestSuiteSystemOutput,
  fetchTestResultsProcessing,
  axiosInstance,
  axiosInstanceWithoutCache
};
