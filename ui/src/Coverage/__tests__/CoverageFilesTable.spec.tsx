import "@testing-library/jest-dom/extend-expect";
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
      createFile("HasLines.tsx", 20),
      createFile("NoLines.tsx", 0),
    ];

    const coverageFiles = {
      files,
    } as CoverageFiles;

    const { queryByText } = render(
      <CoverageFilesTable
        coverageFiles={coverageFiles}
        coverageGroupName="my-group"
      />
    );

    expect(queryByText("dir/HasLines.tsx")).not.toBeNull();
    expect(queryByText("dir/NoLines.tsx")).toBeNull();
  });

  function createFile(fileName: string, totalLines: number) {
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
          coveredPercentage: totalLines > 0 ? 100.0 : 0.0,
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
