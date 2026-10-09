import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import { SlowTestRegressions, TestCase } from "../../../model/TestRunModel";
import SlowTestRegressionSummary from "../SlowTestRegressionSummary";

describe("SlowTestRegressionSummary", () => {
  it("should list tests slower than their baseline", () => {
    const slowTestRegressions: SlowTestRegressions = {
      thresholdPercent: 50,
      baselineRunCount: 10,
      regressions: [
        {
          testCase: {
            idx: 2,
            testSuiteIdx: 1,
            name: "should save",
            packageName: "com.acme",
            className: "RepositoryTest",
            duration: 3.0,
          } as TestCase,
          baselineDuration: 1.0,
          baselineSampleCount: 8,
          durationIncrease: 2.0,
          increasePercent: 200.0,
        },
      ],
    };

    const { getByTestId } = render(
      <SlowTestRegressionSummary
        publicId="SLOWREG1"
        slowTestRegressions={slowTestRegressions}
      />,
    );

    expect(getByTestId("slow-test-regressions-title")).toHaveTextContent(
      "Slower than baseline",
    );
    expect(getByTestId("slow-test-regressions-headline")).toHaveTextContent(
      "1 test more than 50% slower than their baseline",
    );
    expect(getByTestId("slow-test-regressions")).toHaveTextContent(
      "previous 10 CI runs",
    );
    expect(getByTestId("slow-test-regression-change-1-2")).toHaveTextContent(
      "+2.000s (+200%)",
    );
    expect(getByTestId("slow-test-regression-link-1-2")).toHaveTextContent(
      "com.acme.RepositoryTest.should save",
    );
  });
});
