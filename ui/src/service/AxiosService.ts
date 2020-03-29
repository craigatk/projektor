import axios from "axios";
import applyCaseConverters from "axios-case-converter";
import { setupCache } from "axios-cache-adapter";

const cache = setupCache({
  maxAge: 15 * 60 * 1000,
});

const myAxios = axios.create({
  baseURL: process.env.API_BASE_URL,
  adapter: cache.adapter,
});
// @ts-ignore
const axiosInstance = applyCaseConverters(myAxios);

const axiosInstanceWithoutCache = applyCaseConverters(
  // @ts-ignore
  axios.create({
    baseURL: process.env.API_BASE_URL,
  })
);

export { axiosInstance, axiosInstanceWithoutCache };
