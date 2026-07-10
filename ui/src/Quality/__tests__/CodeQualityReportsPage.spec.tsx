import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../service/AxiosService";
import CodeQualityReportsPage from "../CodeQualityReportsPage";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("CodeQualityReportsPage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render code quality reports", async () => {
    const publicId = "TESTRUN1";

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/quality`)
      .reply(200, { reports: [] });

    const { findByTestId } = render(
      <CodeQualityReportsPage publicId={publicId} />,
    );

    expect(await findByTestId("code-quality-title")).toBeInTheDocument();
    expect(
      await findByTestId("code-quality-reports-not-found"),
    ).toBeInTheDocument();
  });

  it("should render an error message when the fetch fails", async () => {
    const publicId = "TESTRUN2";

    mockAxios.onGet(`http://localhost:8080/run/${publicId}/quality`).reply(500);

    const { findByTestId } = render(
      <CodeQualityReportsPage publicId={publicId} />,
    );

    expect(await findByTestId("loading-section-error")).toBeInTheDocument();
  });
});
