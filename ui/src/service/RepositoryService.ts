import { axiosInstance } from "./AxiosService";
import { AxiosResponse } from "axios";
import {
  RepositoryCoverageTimeline,
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

export { fetchRepositoryTimeline, fetchRepositoryCoverageTimeline };
