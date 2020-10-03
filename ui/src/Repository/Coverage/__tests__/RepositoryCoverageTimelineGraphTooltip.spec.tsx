import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import moment from "moment-timezone";
import RepositoryCoverageTimelineGraphTooltip from "../RepositoryCoverageTimelineGraphTooltip";

describe("RepositoryCoverageTimelineGraphTooltip", () => {
  beforeEach(() => {
    // Set the default timezone so it uses the same timezone when
    // running locally and when running in CI.
    moment.tz.setDefault("America/Chicago");
  });

  afterEach(() => {
    moment.tz.setDefault();
  });

  it("should render covered percentages and run date", () => {
    const payload = {
      createdTimestamp: moment.utc("2020-10-02T11:03:04.580Z").toDate(),
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
      "Oct 2nd 2020 6:03 am"
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
