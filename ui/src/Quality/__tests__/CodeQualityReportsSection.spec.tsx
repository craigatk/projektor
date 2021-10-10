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
          contents: "Code Quality Report 1",
          fileName: "file_1.txt",
        } as CodeQualityReport,
        {
          contents: "",
          fileName: "empty.txt",
        } as CodeQualityReport,
        {
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

  it("should show message when no code quality reports found", () => {
    const codeQualityReports = {
      reports: [
        {
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

    expect(queryByTestId("code-quality-reports-not-found")).not.toBeNull();
  });
});
