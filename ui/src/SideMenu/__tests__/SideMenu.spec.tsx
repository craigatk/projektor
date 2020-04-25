import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import SideMenu from "../SideMenu";
import { TestRunSummary } from "../../model/TestRunModel";
import { PinState } from "../../Pin/PinState";

describe("SideMenu", () => {
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
      slowestTestCaseDuration: 5.0,
    } as TestRunSummary;

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={true}
        />
      </PinState>
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
      slowestTestCaseDuration: 5.0,
    } as TestRunSummary;

    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={false}
        />
      </PinState>
    );

    expect(queryByTestId("nav-link-attachments")).toBeNull();
  });
});
