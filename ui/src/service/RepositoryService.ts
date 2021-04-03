import { axiosInstance } from "./AxiosService";
import { AxiosResponse } from "axios";
import {
  RepositoryCoverageTimeline,
  RepositoryFlakyTests,
  RepositoryPerformanceTimeline,
  RepositoryTimeline,
} from "../model/RepositoryModel";

const fetchRepositoryTimeline = (
  repoName: string,
  projectName?: string
): Promise<AxiosResponse<RepositoryTimeline>> => {
  const url = projectName
    ? `repo/${repoName}/project/${projectName}/timeline`
    : `repo/${repoName}/timeline`;
  // @ts-ignore
  return axiosInstance.get<RepositoryTimeline>(url);
};

const fetchRepositoryCoverageTimeline = (
  repoName: string,
  projectName?: string
): Promise<AxiosResponse<RepositoryCoverageTimeline>> => {
  const url = projectName
    ? `repo/${repoName}/project/${projectName}/coverage/timeline`
    : `repo/${repoName}/coverage/timeline`;
  // @ts-ignore
  return axiosInstance.get<RepositoryCoverageTimeline>(url);
};

const fetchRepositoryCoverageBadge = (
  repoName: string,
  projectName?: string
): Promise<AxiosResponse<string>> => {
  const url = projectName
    ? `repo/${repoName}/project/${projectName}/badge/coverage`
    : `repo/${repoName}/badge/coverage`;
  // @ts-ignore
  return axiosInstance.get<string>(url);
};

const fetchRepositoryFlakyTests = (
  maxRuns: number,
  flakyThreshold: number,
  branchType: string,
  repoName: string,
  projectName?: string
): Promise<AxiosResponse<RepositoryFlakyTests>> => {
  const branchTypeValue = branchType.toUpperCase();

  const url = projectName
    ? `repo/${repoName}/project/${projectName}/tests/flaky?threshold=${flakyThreshold}&max_runs=${maxRuns}&branch_type=${branchTypeValue}`
    : `repo/${repoName}/tests/flaky?threshold=${flakyThreshold}&max_runs=${maxRuns}&branch_type=${branchTypeValue}`;
  // @ts-ignore
  return axiosInstance.get<RepositoryFlakyTests>(url);
};

const fetchRepositoryPerformanceTimeline = (
  repoName: string,
  projectName?: string
): Promise<AxiosResponse<RepositoryPerformanceTimeline>> => {
  const url = projectName
    ? `repo/${repoName}/project/${projectName}/performance/timeline`
    : `repo/${repoName}/performance/timeline`;
  // @ts-ignore
  return axiosInstance.get<RepositoryPerformanceTimeline>(url);
};

export {
  fetchRepositoryTimeline,
  fetchRepositoryCoverageTimeline,
  fetchRepositoryCoverageBadge,
  fetchRepositoryFlakyTests,
  fetchRepositoryPerformanceTimeline,
};
