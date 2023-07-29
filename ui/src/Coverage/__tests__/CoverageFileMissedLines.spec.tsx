import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { act, render } from "@testing-library/react";
import CoverageFileMissedLines from "../CoverageFileMissedLines";

describe("CoverageFileMissedLines", () => {
  it("should show all lines when exactly 10", () => {
    const missedLines = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    const fileIdx = 1;
    const filePath = "src/MyFile.tsx";

    const { getByTestId } = render(
      <CoverageFileMissedLines
        missedLines={missedLines}
        filePath={filePath}
        fileIdx={fileIdx}
      />,
    );

    expect(getByTestId("coverage-file-missed-lines-1")).toHaveTextContent(
      "1, 2, 3, 4, 5, 6, 7, 8, 9, 10",
    );
  });

  it("should only show first 10 lines when more than 10", () => {
    const missedLines = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
    const fileIdx = 1;
    const filePath = "src/MyFile.tsx";

    const { getByTestId } = render(
      <CoverageFileMissedLines
        missedLines={missedLines}
        filePath={filePath}
        fileIdx={fileIdx}
      />,
    );
    expect(getByTestId("coverage-file-missed-lines-1")).toHaveTextContent(
      "1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ... show all",
    );

    act(() => {
      getByTestId("coverage-file-1-show-all-missed-lines-link").click();
    });

    expect(getByTestId("coverage-file-missed-lines-1")).toHaveTextContent(
      "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12",
    );
  });
});
