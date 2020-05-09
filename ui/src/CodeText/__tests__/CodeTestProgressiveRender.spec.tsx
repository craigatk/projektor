import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { getNodeText, render } from "@testing-library/react";
import CodeTextProgressiveRender from "../CodeTextProgressiveRender";
import { act } from "react-dom/test-utils";

describe("CodeTextProgressiveRender", () => {
  it("should render lines progressively", async () => {
    const lines = ["line1", "line2", "line3", "line4", "line5", "line6"];

    const isLineHighlighted = jest.fn();
    const handleLineClick = jest.fn();
    const renderComplete = jest.fn();

    jest.useFakeTimers();

    const { findByTestId, queryByTestId } = render(
      <CodeTextProgressiveRender
        lines={lines}
        isLineHighlighted={isLineHighlighted}
        handleLineClick={handleLineClick}
        highlightedLine={null}
        highlightedRangeEnd={null}
        renderComplete={renderComplete}
        pageSize={2}
      />
    );

    expect(getNodeText(await findByTestId("code-text-line-content-1"))).toBe(
      "line1"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-2"))).toBe(
      "line2"
    );
    expect(queryByTestId("code-text-line-content-3")).toBeNull();
    expect(queryByTestId("code-text-line-content-4")).toBeNull();
    expect(queryByTestId("code-text-line-content-5")).toBeNull();
    expect(queryByTestId("code-text-line-content-6")).toBeNull();

    act(() => jest.runOnlyPendingTimers());

    expect(getNodeText(await findByTestId("code-text-line-content-1"))).toBe(
      "line1"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-2"))).toBe(
      "line2"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-3"))).toBe(
      "line3"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-4"))).toBe(
      "line4"
    );
    expect(queryByTestId("code-text-line-content-5")).toBeNull();
    expect(queryByTestId("code-text-line-content-6")).toBeNull();

    act(() => jest.runOnlyPendingTimers());

    expect(getNodeText(await findByTestId("code-text-line-content-1"))).toBe(
      "line1"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-2"))).toBe(
      "line2"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-3"))).toBe(
      "line3"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-4"))).toBe(
      "line4"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-5"))).toBe(
      "line5"
    );
    expect(getNodeText(await findByTestId("code-text-line-content-6"))).toBe(
      "line6"
    );

    act(() => jest.runOnlyPendingTimers());

    expect(renderComplete).toHaveBeenCalled();
  });
});
