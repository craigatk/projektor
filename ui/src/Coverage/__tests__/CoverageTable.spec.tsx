import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import CoverageTable, { CoverageTableRow } from "../CoverageTable";
import { CoverageStat, CoverageStats } from "../../model/TestRunModel";

describe("CoverageTable", () => {
  it("should allow sorting by line, branch, or statement covered percentage", () => {
    const row1 = {
      name: "row1",
      stats: {
        branchStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 50,
        } as CoverageStat,
        lineStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 60,
        } as CoverageStat,
        statementStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 70,
        } as CoverageStat,
      } as CoverageStats,
    } as CoverageTableRow;

    const row2 = {
      name: "row2",
      stats: {
        branchStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 80,
        } as CoverageStat,
        lineStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 70,
        } as CoverageStat,
        statementStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 50,
        } as CoverageStat,
      } as CoverageStats,
    } as CoverageTableRow;

    const row3 = {
      name: "row3",
      stats: {
        branchStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 10,
        } as CoverageStat,
        lineStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 90,
        } as CoverageStat,
        statementStat: {
          covered: 1,
          missed: 1,
          total: 1,
          coveredPercentage: 20,
        } as CoverageStat,
      } as CoverageStats,
    } as CoverageTableRow;

    const { getByText, getAllByTestId } = render(
      <CoverageTable
        rows={[row1, row2, row3]}
        pageTitle="Coverage"
        groupHeader="Group"
      />
    );

    getByText("Line").click(); // Initial sort is ascending

    const lineCoverageNumbers = getAllByTestId("line-coverage", {
      exact: false,
    });

    expect(lineCoverageNumbers.length).toBe(3);
    expect(lineCoverageNumbers[0]).toHaveTextContent("60%");
    expect(lineCoverageNumbers[1]).toHaveTextContent("70%");
    expect(lineCoverageNumbers[2]).toHaveTextContent("90%");

    getByText("Branch").click(); // Initial sort is ascending

    const branchCoverageNumbers = getAllByTestId("branch-coverage", {
      exact: false,
    });

    expect(branchCoverageNumbers.length).toBe(3);
    expect(branchCoverageNumbers[0]).toHaveTextContent("10%");
    expect(branchCoverageNumbers[1]).toHaveTextContent("50%");
    expect(branchCoverageNumbers[2]).toHaveTextContent("80%");

    getByText("Statement").click(); // Initial sort is ascending

    const statementCoverageNumbers = getAllByTestId("statement-coverage", {
      exact: false,
    });

    expect(statementCoverageNumbers.length).toBe(3);
    expect(statementCoverageNumbers[0]).toHaveTextContent("20%");
    expect(statementCoverageNumbers[1]).toHaveTextContent("50%");
    expect(statementCoverageNumbers[2]).toHaveTextContent("70%");
  });
});
