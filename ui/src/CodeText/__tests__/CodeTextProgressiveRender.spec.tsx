import "@testing-library/jest-dom/extend-expect";
import React from "react";
import {getNodeText, render, waitFor} from "@testing-library/react";
import CodeTextProgressiveRender from "../CodeTextProgressiveRender";
import { act } from "react-dom/test-utils";
import _ from "lodash";
import CodeTextLinesChunk from "../CodeTextLinesChunk";

describe("CodeTextProgressiveRender", () => {
  const numLines = 10;
  const lines = _.times(numLines, (idx) => `line${idx + 1}`);
  const progressBarId = "code-text-progress-render-loading-indicator";

  it("should render lines progressively", async () => {
    const lineElements = lines.map((line, idx) => (
      <div data-testid={`line-${idx + 1}`} key={`line-${idx + 1}`}>
        {line}
      </div>
    ));

    jest.useFakeTimers();

    const pageSize = 2;
    const lineChunks = _.chunk(lineElements, pageSize).map((lines, idx) => (
      <CodeTextLinesChunk lines={lines} key={`chunk-${idx}`} />
    ));

    const { findByTestId, queryByTestId } = render(
      <CodeTextProgressiveRender
        lineChunks={lineChunks}
        lineCount={lines.length}
        lineHeight={14}
        highlightedLine={2}
      />
    );

    expect(queryByTestId(progressBarId)).not.toBeNull();
    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(queryByTestId("line-3")).toBeNull();
    expect(queryByTestId("line-4")).toBeNull();
    expect(queryByTestId("line-5")).toBeNull();
    expect(queryByTestId("line-6")).toBeNull();
    expect(queryByTestId("line-7")).toBeNull();
    expect(queryByTestId("line-8")).toBeNull();
    expect(queryByTestId("line-9")).toBeNull();
    expect(queryByTestId("line-10")).toBeNull();

    act(() => {
      jest.runOnlyPendingTimers();
    });

    expect(queryByTestId(progressBarId)).not.toBeNull();
    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(queryByTestId("line-5")).toBeNull();
    expect(queryByTestId("line-6")).toBeNull();
    expect(queryByTestId("line-7")).toBeNull();
    expect(queryByTestId("line-8")).toBeNull();
    expect(queryByTestId("line-9")).toBeNull();
    expect(queryByTestId("line-10")).toBeNull();

    act(() => {
      jest.runOnlyPendingTimers();
    });

    expect(queryByTestId(progressBarId)).not.toBeNull();
    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(getNodeText(await findByTestId("line-5"))).toBe("line5");
    expect(getNodeText(await findByTestId("line-6"))).toBe("line6");
    expect(queryByTestId("line-7")).toBeNull();
    expect(queryByTestId("line-8")).toBeNull();
    expect(queryByTestId("line-9")).toBeNull();
    expect(queryByTestId("line-10")).toBeNull();

    act(() => {
      jest.runOnlyPendingTimers();
    });

    expect(queryByTestId(progressBarId)).not.toBeNull();
    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(getNodeText(await findByTestId("line-5"))).toBe("line5");
    expect(getNodeText(await findByTestId("line-6"))).toBe("line6");
    expect(getNodeText(await findByTestId("line-7"))).toBe("line7");
    expect(getNodeText(await findByTestId("line-8"))).toBe("line8");
    expect(queryByTestId("line-9")).toBeNull();
    expect(queryByTestId("line-10")).toBeNull();

    act(() => {
      jest.runOnlyPendingTimers();
    });

    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-2"))).toBe("line2");
    expect(getNodeText(await findByTestId("line-3"))).toBe("line3");
    expect(getNodeText(await findByTestId("line-4"))).toBe("line4");
    expect(getNodeText(await findByTestId("line-5"))).toBe("line5");
    expect(getNodeText(await findByTestId("line-6"))).toBe("line6");
    expect(getNodeText(await findByTestId("line-7"))).toBe("line7");
    expect(getNodeText(await findByTestId("line-8"))).toBe("line8");
    expect(getNodeText(await findByTestId("line-9"))).toBe("line9");
    expect(getNodeText(await findByTestId("line-10"))).toBe("line10");

    act(() => {
      jest.runOnlyPendingTimers();
    });

    await waitFor(() => expect(queryByTestId(progressBarId)).toBeNull())
  });

  it("should not render progress bar when only 1 chunk", async () => {
    const lineElements = lines.map((line, idx) => (
      <div data-testid={`line-${idx + 1}`} key={`line-${idx + 1}`}>
        {line}
      </div>
    ));

    jest.useFakeTimers();

    const pageSize = numLines;
    const lineChunks = _.chunk(lineElements, pageSize).map((lines, idx) => (
      <CodeTextLinesChunk lines={lines} key={`chunk-${idx}`} />
    ));

    const { findByTestId, queryByTestId } = render(
      <CodeTextProgressiveRender
        lineChunks={lineChunks}
        lineCount={lines.length}
        lineHeight={14}
        highlightedLine={2}
      />
    );

    expect(getNodeText(await findByTestId("line-1"))).toBe("line1");
    expect(getNodeText(await findByTestId("line-10"))).toBe("line10");

    expect(queryByTestId(progressBarId)).toBeNull();
  });
});
