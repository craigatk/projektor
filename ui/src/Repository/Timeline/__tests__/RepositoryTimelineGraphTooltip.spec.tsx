import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import moment from "moment-timezone";
import RepositoryTimelineGraphTooltip from "../RepositoryTimelineGraphTooltip";

describe("RepositoryTimelineGraphTooltip", () => {
  beforeEach(() => {
    // Set the default timezone so it uses the same timezone when
    // running locally and when running in CI.
    moment.tz.setDefault("America/Chicago");
  });

  afterEach(() => {
    moment.tz.setDefault();
  });

  it("should display run date in local timezone", () => {
    const createdTimestamp = moment.utc("2020-10-02T11:03:04.580Z").toDate();
    const duration = 170;
    const totalTestCount = 15;

    const props = {
      payload: [{ payload: { createdTimestamp, duration, totalTestCount } }],
    };

    const { getByTestId } = render(
      <RepositoryTimelineGraphTooltip {...props} />
    );

    expect(getByTestId("timeline-tooltip-run-date")).toHaveTextContent(
      "Oct 2nd 2020, 6:03 am"
    );
  });
});
