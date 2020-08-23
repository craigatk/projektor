import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render, getNodeText } from "@testing-library/react";
import { CoverageStat } from "../../model/TestRunModel";
import CoverageGraph from "../CoverageGraph";

describe("CoverageGraph", () => {
  it("should display coverage data when not inline", () => {
    const coverageStat = {
      covered: 10,
      missed: 1,
      total: 11,
      coveredPercentage: 98.89,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={false}
      />
    );

    expect(getNodeText(queryByTestId("coverage-graph-title-line"))).toBe(
      "Line 98.89%"
    );
  });

  it("should not display title when inline", () => {
    const coverageStat = {
      covered: 10,
      missed: 1,
      total: 11,
      coveredPercentage: 98.89,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={true}
      />
    );

    expect(queryByTestId("coverage-graph-title-line")).toBeNull();
  });

  it("should not display coverage data when there isn't any", () => {
    const coverageStat = {
      covered: 0,
      missed: 0,
      total: 0,
      coveredPercentage: 0,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={false}
      />
    );

    expect(queryByTestId("coverage-graph-title-line")).toBeNull();
  });
});
