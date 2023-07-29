import { axiosInstance } from "./AxiosService";
import { AxiosResponse } from "axios";
import { ResultsProcessingFailure } from "../model/AdminModel";

const fetchRecentFailures = (
  count: number,
): Promise<AxiosResponse<ResultsProcessingFailure[]>> => {
  // @ts-ignore
  return axiosInstance.get<ResultsProcessingFailure[]>(
    `failures/recent?count=${count}`,
  );
};

export { fetchRecentFailures };
