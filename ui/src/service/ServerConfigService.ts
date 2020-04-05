import { AxiosResponse } from "axios";
import { axiosInstanceWithoutCache } from "./AxiosService";
import { ServerConfig } from "../model/ServerConfigModel";

const fetchServerConfig = (): Promise<AxiosResponse<ServerConfig>> =>
  // @ts-ignore
  axiosInstanceWithoutCache.get<ServerConfig>(`/config`);

export { fetchServerConfig };
