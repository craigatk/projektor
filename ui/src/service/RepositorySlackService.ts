import { AxiosResponse } from "axios";
import { axiosInstanceWithoutCache } from "./AxiosService";
import { RepositorySlackChannel } from "../model/SlackModel";
import { repositoryLinkUrlAPI } from "../Repository/RepositoryLink";

const slackChannelUrl = (repoName: string, projectName?: string): string =>
  repositoryLinkUrlAPI(repoName, projectName, "notification/slack");

const fetchRepositorySlackChannel = (
  repoName: string,
  projectName?: string,
): Promise<AxiosResponse<RepositorySlackChannel>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<RepositorySlackChannel>(
    slackChannelUrl(repoName, projectName),
  );

const saveRepositorySlackChannel = (
  repoName: string,
  projectName: string | undefined,
  channelName: string,
): Promise<AxiosResponse<RepositorySlackChannel>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.put<RepositorySlackChannel>(
    slackChannelUrl(repoName, projectName),
    { channelName },
  );

const deleteRepositorySlackChannel = (
  repoName: string,
  projectName?: string,
): Promise<AxiosResponse<void>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.delete(slackChannelUrl(repoName, projectName));

export {
  fetchRepositorySlackChannel,
  saveRepositorySlackChannel,
  deleteRepositorySlackChannel,
};
