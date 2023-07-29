import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
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
      />,
    );

    expect(queryByTestId("coverage-graph-title-line")).toHaveTextContent(
      "Line 98.89%",
    );
  });

  it("should display positive previous coverage", () => {
    const coverageStat = {
      covered: 10,
      missed: 1,
      total: 11,
      coveredPercentage: 98.89,
      coveredPercentageDelta: 1.25,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={false}
      />,
    );

    expect(queryByTestId("coverage-graph-title-line")).toHaveTextContent(
      "Line 98.89% +1.25%",
    );
  });

  it("should display negative previous coverage", () => {
    const coverageStat = {
      covered: 10,
      missed: 1,
      total: 11,
      coveredPercentage: 98.89,
      coveredPercentageDelta: -2.25,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={false}
      />,
    );

    expect(queryByTestId("coverage-graph-title-line")).toHaveTextContent(
      "Line 98.89% -2.25%",
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
      />,
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
      />,
    );

    expect(queryByTestId("coverage-graph-title-line")).toBeNull();
  });

  it("graph should have link when specified", () => {
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
        testIdPrefix="graph"
        coveredPercentageLink="/tests/ABC123/"
      />,
    );

    expect(queryByTestId("graph-covered-percentage-link")).toBeInTheDocument();
  });

  it("when coverage percentage less than 50% should include 'Uncovered' in label", () => {
    const coverageStat = {
      covered: 4,
      missed: 6,
      total: 10,
      coveredPercentage: 40.0,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={true}
        testIdPrefix="graph"
        coveredPercentageLink="/tests/ABC123/"
      />,
    );

    expect(queryByTestId("graph-uncovered-line-count")).toHaveTextContent(
      "6 Uncovered",
    );
  });

  it("when coverage percentage more than 50% should not include 'Uncovered' in label", () => {
    const coverageStat = {
      covered: 6,
      missed: 4,
      total: 10,
      coveredPercentage: 60.0,
    } as CoverageStat;

    const type = "Line";

    const { queryByTestId } = render(
      <CoverageGraph
        coverageStat={coverageStat}
        type={type}
        height={25}
        inline={true}
        testIdPrefix="graph"
        coveredPercentageLink="/tests/ABC123/"
      />,
    );

    expect(queryByTestId("graph-uncovered-line-count")).toHaveTextContent("4");
  });

  it("graph should not have link when not specified", () => {
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
        testIdPrefix="graph"
        coveredPercentageLink={null}
      />,
    );

    expect(
      queryByTestId("graph-covered-percentage-link"),
    ).not.toBeInTheDocument();
  });
});
