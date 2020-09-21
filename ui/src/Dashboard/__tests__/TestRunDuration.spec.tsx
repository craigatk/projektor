import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import TestRunDuration from "../TestRunDuration";

describe("TestRunDuration", () => {
  it("should display wall clock duration when it is set", () => {
    const publicId = "12345";
    const averageDuration = 2.5;
    const cumulativeDuration = 10;
    const wallClockDuration = 8.75;
    const slowestTestCaseDuration = 1.25;

    const { getByTestId } = render(
      <TestRunDuration
        publicId={publicId}
        averageDuration={averageDuration}
        cumulativeDuration={cumulativeDuration}
        wallClockDuration={wallClockDuration}
        slowestTestCaseDuration={slowestTestCaseDuration}
      />
    );

    expect(getByTestId("test-run-wall-clock-duration")).toHaveTextContent(
      "8.75s"
    );
  });

  it("should not display wall clock duration when it is not set", () => {
    const publicId = "12345";
    const averageDuration = 2.5;
    const cumulativeDuration = 10;
    const wallClockDuration = null;
    const slowestTestCaseDuration = 1.25;

    const { queryByTestId } = render(
      <TestRunDuration
        publicId={publicId}
        averageDuration={averageDuration}
        cumulativeDuration={cumulativeDuration}
        wallClockDuration={wallClockDuration}
        slowestTestCaseDuration={slowestTestCaseDuration}
      />
    );

    expect(queryByTestId("test-run-wall-clock-duration")).toBeNull();
  });
});
