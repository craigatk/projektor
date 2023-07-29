import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import CodeText from "../CodeText";
import { globalHistory } from "@reach/router";
import { QueryParamProvider } from "use-query-params";

describe("CodeText", () => {
  it("should split output into lines", async () => {
    const text = "line 1\nline 2";

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <CodeText text={text} />
      </QueryParamProvider>,
    );

    await findByTestId("code-text");

    expect(await findByTestId("code-text-line-content-1")).toHaveTextContent(
      "line 1",
    );
    expect(await findByTestId("code-text-line-number-1")).toHaveTextContent(
      "1",
    );

    expect(await findByTestId("code-text-line-content-2")).toHaveTextContent(
      "line 2",
    );
    expect(await findByTestId("code-text-line-number-2")).toHaveTextContent(
      "2",
    );

    expect(queryByTestId("code-text-line-number-3")).toBeNull();
  });

  it("when output is blank should not show any lines", async () => {
    const text = "";

    const { findByTestId, queryByTestId } = render(
      <QueryParamProvider reachHistory={globalHistory}>
        <CodeText text={text} />
      </QueryParamProvider>,
    );

    await findByTestId("code-text");

    expect(queryByTestId("code-text-line-number-1")).toBeNull();
  });
});
