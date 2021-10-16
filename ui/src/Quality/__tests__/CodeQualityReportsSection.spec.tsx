import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { queryByText, render } from "@testing-library/react";
import {
  CodeQualityReport,
  CodeQualityReports,
} from "../../model/TestRunModel";
import CodeQualityReportsSection from "../CodeQualityReportsSection";

describe("CodeQualityReportsSection", () => {
  it("should filter out reports that are empty", () => {
    const codeQualityReports = {
      reports: [
        {
          idx: 1,
          contents: "Code Quality Report 1",
          fileName: "file_1.txt",
        } as CodeQualityReport,
        {
          idx: 2,
          contents: "",
          fileName: "empty.txt",
        } as CodeQualityReport,
        {
          idx: 3,
          contents: "Code Quality Report 2",
          fileName: "file_2.txt",
        } as CodeQualityReport,
      ],
    } as CodeQualityReports;

    const { queryByText } = render(
      <CodeQualityReportsSection
        publicId="12345"
        codeQualityReports={codeQualityReports}
      />
    );

    expect(queryByText("file_1.txt")).not.toBeNull();
    expect(queryByText("file_2.txt")).not.toBeNull();
    expect(queryByText("empty.txt")).toBeNull();
  });

  it("should show message when only passing (empty) reports found", () => {
    const codeQualityReports = {
      reports: [
        {
          idx: 1,
          contents: "",
          fileName: "empty.txt",
        } as CodeQualityReport,
      ],
    } as CodeQualityReports;

    const { queryByTestId } = render(
      <CodeQualityReportsSection
        publicId="12345"
        codeQualityReports={codeQualityReports}
      />
    );

    expect(queryByTestId("code-quality-reports-all-passed")).not.toBeNull();
  });

  it("should show message when no code quality reports found", () => {
    const codeQualityReports = {
      reports: [],
    } as CodeQualityReports;

    const { queryByTestId } = render(
      <CodeQualityReportsSection
        publicId="12345"
        codeQualityReports={codeQualityReports}
      />
    );

    expect(queryByTestId("code-quality-reports-not-found")).not.toBeNull();
  });
});
