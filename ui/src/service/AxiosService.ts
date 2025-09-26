import axios from "axios";
import applyCaseConverters from "axios-case-converter";
import { setupCache } from "axios-cache-interceptor";
import { CacheOptions } from "axios-cache-interceptor/src/cache/create";
import { baseUrl } from "./EnvService";

const cacheOptions: CacheOptions = {
  cacheTakeover: false,
  ttl: 15 * 60 * 1000,
};

const myAxios = setupCache(
  axios.create({
    baseURL: baseUrl(),
  }),
  cacheOptions,
);
// @ts-ignore
const axiosInstance = applyCaseConverters(myAxios);

const axiosInstanceWithoutCache = applyCaseConverters(
  // @ts-ignore
  axios.create({
    baseURL: baseUrl(),
  }),
);

export { axiosInstance, axiosInstanceWithoutCache };
