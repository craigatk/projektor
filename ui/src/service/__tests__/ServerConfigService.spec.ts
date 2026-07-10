import MockAdapter from "axios-mock-adapter";
import { axiosInstanceWithoutCache } from "../AxiosService";
import { fetchServerConfig } from "../ServerConfigService";

vi.mock("../EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("ServerConfigService", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstanceWithoutCache);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch the server config", async () => {
    const serverConfig = {
      aiConfig: { testCaseFailureAnalysisEnabled: true },
      cleanup: { enabled: true, maxReportAgeInDays: 30 },
    };

    mockAxios
      .onGet("http://localhost:8080/config")
      .reply(200, serverConfig);

    const response = await fetchServerConfig();

    expect(response.data).toEqual(serverConfig);
  });
});
