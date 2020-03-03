import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import TestRunSideMenu from "../TestRunSideMenu";
import { TestRunSummary } from "../../model/TestRunModel";

describe("TestRunSideMenu", () => {
  it("when attachments should show attachments link", () => {
    const publicId = "34567";
    const testRunSummary = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: false,
      cumulativeDuration: 10.0,
      averageDuration: 2.5,
      slowestTestCaseDuration: 5.0
    } as TestRunSummary;

    const { queryByTestId } = render(
      <TestRunSideMenu
        publicId={publicId}
        testRunSummary={testRunSummary}
        hasAttachments={true}
      />
    );

    expect(queryByTestId("nav-link-attachments")).not.toBeNull();
  });

  it("when no attachments should not show attachments link", () => {
    const publicId = "34567";
    const testRunSummary = {
      id: publicId,
      totalTestCount: 4,
      totalPassingCount: 2,
      totalSkippedCount: 1,
      totalFailureCount: 1,
      passed: false,
      cumulativeDuration: 10.0,
      averageDuration: 2.5,
      slowestTestCaseDuration: 5.0
    } as TestRunSummary;

    const { queryByTestId } = render(
      <TestRunSideMenu
        publicId={publicId}
        testRunSummary={testRunSummary}
        hasAttachments={false}
      />
    );

    expect(queryByTestId("nav-link-attachments")).toBeNull();
  });
});
