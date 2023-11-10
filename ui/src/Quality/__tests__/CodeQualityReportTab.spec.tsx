import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { queryByText, render } from "@testing-library/react";
import { CodeQualityReport } from "../../model/TestRunModel";
import CodeQualityReportTab from "../CodeQualityReportTab";
import { globalHistory } from "@reach/router";
import { QueryParamProvider } from "use-query-params";

describe("CodeQualityReportTab", () => {
  it("should show report contents for given report index", () => {
    const reports = [
      {
        idx: 1,
        fileName: "file_1.txt",
        contents: "Contents 1",
      } as CodeQualityReport,
      {
        idx: 2,
        fileName: "file_2.txt",
        contents: "Contents 2",
      } as CodeQualityReport,
      {
        idx: 3,
        fileName: "file_3.txt",
        contents: "Contents 3",
      } as CodeQualityReport,
    ];

    const { queryByText } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <CodeQualityReportTab
          codeQualityReportsWithContents={reports}
          idx="2"
        />
      </QueryParamProvider>,
    );

    expect(queryByText("Contents 2")).not.toBeNull();
  });
});
