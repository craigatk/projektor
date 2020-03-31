import { AxiosResponse } from "axios";
import { axiosInstanceWithoutCache } from "./AxiosService";
import { ServerConfig } from "../model/ServerConfigModel";

const fetchServerConfig = (
  publicId: string
): Promise<AxiosResponse<ServerConfig>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<ServerConfig>(`/run/${publicId}/config`);

export { fetchServerConfig };
