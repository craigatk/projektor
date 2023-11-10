import { axiosInstance } from "./AxiosService";
import { AxiosResponse } from "axios";
import { OrganizationCoverage } from "../model/OrganizationModel";

const fetchOrganizationCoverage = (
  orgName: String,
): Promise<AxiosResponse<OrganizationCoverage>> => {
  // @ts-ignore
  return axiosInstance.get<OrganizationCoverage>(`org/${orgName}/coverage`);
};

export { fetchOrganizationCoverage };
