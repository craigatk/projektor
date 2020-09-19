import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import moment from "moment";
import RepositoryCoverageTimelineGraphTooltip from "../RepositoryCoverageTimelineGraphTooltip";

describe("RepositoryCoverageTimelineGraphTooltip", () => {
  it("should render covered percentages and run date", () => {
    const payload = {
      date: moment("2020-09-12").toDate(),
      lineValue: 97.25,
      branchValue: 80.25,
    };

    const props = {
      payload: [{ payload }],
    };

    const { getByTestId } = render(
      <RepositoryCoverageTimelineGraphTooltip {...props} />
    );

    expect(getByTestId("tooltip-line-coverage-percentage")).toHaveTextContent(
      "97.25%"
    );
    expect(getByTestId("tooltip-branch-coverage-percentage")).toHaveTextContent(
      "80.25%"
    );
    expect(getByTestId("tooltip-run-date")).toHaveTextContent(
      "Sep 12th 2020 12:00 am"
    );
  });

  it("when no payload should render empty span", () => {
    const props = {
      payload: [],
    };

    const { getByTestId } = render(
      <RepositoryCoverageTimelineGraphTooltip {...props} />
    );

    expect(getByTestId("empty-tooltip")).toBeInTheDocument();
  });
});
