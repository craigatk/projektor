import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { getNodeText, render } from "@testing-library/react";
import CodeTextProgressiveRender from "../CodeTextProgressiveRender";
import { act } from "react-dom/test-utils";

describe("CodeTextProgressiveRender", () => {
  it("should render lines progressively", async () => {
    const lines = ["line1", "line2", "line3", "line4", "line5", "line6"];
    const listElements = lines.map((line, idx) => (
      <div data-testid={`line-${idx + 1}`} key={`line-${idx + 1}`}>
        {line}
      </div>
    ));

    jest.useFakeTimers();

    const { findByTestId, queryByTestId } = render(
      <CodeTextProgressiveRender
        listElements={listElements}
        pageSize={2}
        lineHeight={14}
        highlightedLine={2}
      />
    );

    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(queryByTestId("line-3")).toBeNull();
    expect(queryByTestId("line-4")).toBeNull();
    expect(queryByTestId("line-5")).toBeNull();
    expect(queryByTestId("line-6")).toBeNull();

    act(() => jest.runOnlyPendingTimers());

    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(queryByTestId("line-5")).toBeNull();
    expect(queryByTestId("line-6")).toBeNull();

    act(() => jest.runOnlyPendingTimers());

    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(getNodeText(await findByTestId("line-5"))).toBe("line5");
    expect(getNodeText(await findByTestId("line-6"))).toBe("line6");

    act(() => jest.runOnlyPendingTimers());
  });
});
