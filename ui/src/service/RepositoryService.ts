import { axiosInstance } from "./AxiosService";
import { AxiosResponse } from "axios";
import { RepositoryCoverageTimeline } from "../model/RepositoryModel";

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

export { fetchRepositoryCoverageTimeline };
