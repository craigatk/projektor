jest.mock("@reach/router");

import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import RepositoryGraphActiveDot from "../RepositoryGraphActiveDot";
import { navigate } from "@reach/router";

describe("RepositoryGraphActiveDot", () => {
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

    const { getByRole } = render(<RepositoryGraphActiveDot {...props} />);

    getByRole(`active-dot-lineValue-${publicId}`).click();

    expect(navigate).toHaveBeenCalledWith(`/tests/${publicId}`);
  });
});
