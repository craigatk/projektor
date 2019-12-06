import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render, fireEvent } from "@testing-library/react";
import CodeTextLine from "../CodeTextLine";

describe("Code text line", () => {
  it("should call line click handler when line is clicked", () => {
    const line = "Code line";
    const idx = 2;
    const highlighted = false;
    const handleLineClick = jest.fn();
    const { getByTestId } = render(
      <CodeTextLine
        line={line}
        idx={idx}
        highlighted={highlighted}
        handleLineClick={handleLineClick}
      />
    );

    fireEvent.click(getByTestId("code-text-line-2"));

    expect(handleLineClick).toHaveBeenCalled();
  });
});
