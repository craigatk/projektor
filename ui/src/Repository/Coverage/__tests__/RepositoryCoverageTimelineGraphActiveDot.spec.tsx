jest.mock("@reach/router");

import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import RepositoryCoverageTimelineGraphActiveDot from "../RepositoryCoverageTimelineGraphActiveDot";
import { navigate } from "@reach/router";

describe("RepositoryCoverageTimelineGraphActiveDot", () => {
  beforeEach(() => {
    // @ts-ignore
    navigate.mockReset();
  });

  it("should link to test run", () => {
    const publicId = "DOT12345";

    const props = {
      cy: 12,
      cx: 12,
      fill: "blue",
      dataKey: "lineValue",
      payload: {
        publicId,
      },
    };

    const { getByRole } = render(
      <RepositoryCoverageTimelineGraphActiveDot {...props} />
    );

    getByRole(`active-dot-lineValue-${publicId}`).click();

    expect(navigate).toHaveBeenCalledWith(`/tests/${publicId}`);
  });
});
