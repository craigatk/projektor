import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../service/AxiosService";
import * as React from "react";
import { render } from "@testing-library/react";
import CodeQualityDashboardSummary from "../CodeQualityDashboardSummary";
import {
  CodeQualityReport,
  CodeQualityReports,
} from "../../model/TestRunModel";

describe("CodeQualityDashboardSummary", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });
  afterEach(() => {
    mockAxios.restore();
  });

  const publicId = "12345";

  it("should show code quality links when there are reports", async () => {
    const codeQualityReports = {
      reports: [
        {
          idx: 1,
          fileName: "report.txt",
          contents: "Report contents",
        } as CodeQualityReport,
      ],
    } as CodeQualityReports;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/quality`)
      .reply(200, codeQualityReports);

    const { findByTestId, findByText } = render(
      <CodeQualityDashboardSummary publicId={publicId} />,
    );

    await findByTestId("code-quality-summary-title");
    await findByText("report.txt");
  });

  it("should not show code quality section when there are no code quality reports", () => {
    const codeQualityReports = {
      reports: [],
    } as CodeQualityReports;

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/quality`)
      .reply(200, codeQualityReports);

    const { queryByTestId } = render(
      <CodeQualityDashboardSummary publicId={publicId} />,
    );

    expect(queryByTestId("code-quality-summary-title")).toBeNull();
  });
});
