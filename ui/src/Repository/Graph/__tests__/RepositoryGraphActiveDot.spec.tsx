jest.mock("@reach/router");

import "@testing-library/jest-dom";
import React from "react";
import { fireEvent, render } from "@testing-library/react";
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

    fireEvent.mouseUp(getByRole(`active-dot-lineValue-${publicId}`));

    expect(navigate).toHaveBeenCalledWith(`/tests/${publicId}`);
  });
});
