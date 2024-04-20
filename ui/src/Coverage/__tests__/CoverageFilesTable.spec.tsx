import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import {
  CoverageFile,
  CoverageFiles,
  CoverageStat,
  CoverageStats,
} from "../../model/TestRunModel";
import CoverageFilesTable from "../CoverageFilesTable";

describe("CoverageFilesTable", () => {
  it("should filter out files without any lines", () => {
    const files = [
      createFile("HasLines.tsx", 20, 100.0),
      createFile("NoLines.tsx", 0, 0.0),
    ];

    const coverageFiles = {
      files,
    } as CoverageFiles;

    const { queryByText } = render(
      <CoverageFilesTable
        coverageFiles={coverageFiles}
        coverageGroupName="my-group"
      />,
    );

    expect(queryByText("dir/HasLines.tsx")).not.toBeNull();
    expect(queryByText("dir/NoLines.tsx")).toBeNull();
  });

  it("should sort by line covered percentage least to highest", () => {
    const files = [
      createFile("MidCoverage.tsx", 20, 50.0),
      createFile("HighCoverage.tsx", 20, 100.0),
      createFile("LowCoverage.tsx", 20, 25.0),
    ];

    const coverageFiles = {
      files,
    } as CoverageFiles;

    const { getByTestId } = render(
      <CoverageFilesTable
        coverageFiles={coverageFiles}
        coverageGroupName="my-group"
      />,
    );

    expect(getByTestId("coverage-file-name-1")).toHaveTextContent(
      "dir/LowCoverage.tsx",
    );
    expect(getByTestId("coverage-file-name-2")).toHaveTextContent(
      "dir/MidCoverage.tsx",
    );
    expect(getByTestId("coverage-file-name-3")).toHaveTextContent(
      "dir/HighCoverage.tsx",
    );
  });

  function createFile(
    fileName: string,
    totalLines: number,
    lineCoveredPercentage: number,
  ) {
    return {
      fileName,
      directoryName: "dir",
      missedLines: [],
      partialLines: [],
      stats: {
        lineStat: {
          covered: totalLines,
          missed: 0,
          total: totalLines,
          coveredPercentage: lineCoveredPercentage,
        } as CoverageStat,
        branchStat: {
          covered: 10,
          missed: 1,
          total: 11,
          coveredPercentage: 98.89,
        } as CoverageStat,
        statementStat: {
          covered: 10,
          missed: 1,
          total: 11,
          coveredPercentage: 98.89,
        } as CoverageStat,
      } as CoverageStats,
    } as CoverageFile;
  }
});
