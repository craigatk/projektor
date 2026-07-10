import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../AxiosService";
import { fetchRecentFailures } from "../AdminService";
import { FailureBodyType } from "../../model/AdminModel";

vi.mock("../EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("AdminService", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch recent failures for the given count", async () => {
    const failures = [
      {
        id: "FAIL1",
        body: "some failure body",
        bodyType: FailureBodyType.COVERAGE,
        createdTimestamp: "2026-07-01T12:00:00.000Z",
      },
    ];

    mockAxios
      .onGet("http://localhost:8080/failures/recent?count=25")
      .reply(200, failures);

    const response = await fetchRecentFailures(25);

    expect(response.data).toEqual(failures);
  });
});
