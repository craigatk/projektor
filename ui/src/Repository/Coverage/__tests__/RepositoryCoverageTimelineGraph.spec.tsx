import "@testing-library/jest-dom";
import React from "react";
import { render, waitFor } from "@testing-library/react";
import {
  RepositoryCoverageTimeline,
  RepositoryCoverageTimelineEntry,
} from "../../../model/RepositoryModel";
import moment from "moment";
import { createCoverageStats } from "../../../testUtils/coverageTestUtils";
import RepositoryCoverageTimelineGraph from "../RepositoryCoverageTimelineGraph";
import ResizeObserver from "resize-observer-polyfill";

window.ResizeObserver = ResizeObserver;

describe("RepositoryCoverageTimelineGraph", () => {
  jest.setTimeout(30000);

  it("should display coverage timeline graph", async () => {
    const timeline = {
      timelineEntries: [
        {
          publicId: "COV1",
          createdTimestamp: moment("2020-09-10").toDate(),
          coverageStats: createCoverageStats(89.01),
        } as RepositoryCoverageTimelineEntry,
        {
          publicId: "COV2",
          createdTimestamp: moment("2020-09-11").toDate(),
          coverageStats: createCoverageStats(90.02),
        } as RepositoryCoverageTimelineEntry,
        {
          publicId: "COV3",
          createdTimestamp: moment("2020-09-12").toDate(),
          coverageStats: createCoverageStats(91.03),
        } as RepositoryCoverageTimelineEntry,
      ],
    } as RepositoryCoverageTimeline;

    const { findByTestId, getByRole } = render(
      <RepositoryCoverageTimelineGraph
        coverageTimeline={timeline}
        graphWidth={500}
      />,
    );

    await findByTestId("repository-coverage-timeline-graph");

    const lineDot1 = await waitFor(() => getByRole(`dot-lineValue-COV1`), {
      timeout: 10000,
    });
    expect(lineDot1).toHaveAttribute("name", "dot-lineValue-89.01");

    const lineDot2 = await waitFor(() => getByRole(`dot-lineValue-COV2`), {
      timeout: 10000,
    });
    expect(lineDot2).toHaveAttribute("name", "dot-lineValue-90.02");

    const lineDot3 = await waitFor(() => getByRole(`dot-lineValue-COV3`), {
      timeout: 10000,
    });
    expect(lineDot3).toHaveAttribute("name", "dot-lineValue-91.03");

    const branchDot1 = await waitFor(() => getByRole(`dot-branchValue-COV1`), {
      timeout: 10000,
    });
    expect(branchDot1).toHaveAttribute("name", "dot-branchValue-90.01");

    const branchDot2 = await waitFor(() => getByRole(`dot-branchValue-COV2`), {
      timeout: 10000,
    });
    expect(branchDot2).toHaveAttribute("name", "dot-branchValue-91.02");

    const branchDot3 = await waitFor(() => getByRole(`dot-branchValue-COV3`), {
      timeout: 10000,
    });
    expect(branchDot3).toHaveAttribute("name", "dot-branchValue-92.03");
  });
});
