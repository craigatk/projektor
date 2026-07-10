import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../AxiosService";
import { fetchOrganizationCoverage } from "../OrganizationService";

vi.mock("../EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("OrganizationService", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch coverage for the given organization", async () => {
    const organizationCoverage = {
      repositories: [
        {
          publicId: "REPO1",
          repoName: "my-repo",
        },
      ],
    };

    mockAxios
      .onGet("http://localhost:8080/org/my-org/coverage")
      .reply(200, organizationCoverage);

    const response = await fetchOrganizationCoverage("my-org");

    expect(response.data).toEqual(organizationCoverage);
  });
});
