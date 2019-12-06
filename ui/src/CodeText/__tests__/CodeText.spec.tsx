import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render, getNodeText } from "@testing-library/react";
import CodeText from "../CodeText";

describe("CodeText", () => {
  it("should split output into lines", () => {
    const text = "line 1\nline 2";

    const { getByTestId, queryByTestId } = render(<CodeText text={text} />);

    expect(getNodeText(getByTestId("code-text-line-content-1"))).toBe("line 1");
    expect(getNodeText(getByTestId("code-text-line-number-1"))).toBe("1");

    expect(getNodeText(getByTestId("code-text-line-content-2"))).toBe("line 2");
    expect(getNodeText(getByTestId("code-text-line-number-2"))).toBe("2");

    expect(queryByTestId("code-text-line-number-3")).toBeNull();
  });

  it("when output is blank should not show any lines", () => {
    const text = "";

    const { queryByTestId } = render(<CodeText text={text} />);

    expect(queryByTestId("code-text-line-number-1")).toBeNull();
  });
});
