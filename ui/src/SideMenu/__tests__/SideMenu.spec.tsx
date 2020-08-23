import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import SideMenu from "../SideMenu";
import { TestRunSummary } from "../../model/TestRunModel";
import { PinState } from "../../Pin/PinState";

describe("SideMenu", () => {
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

  it("when attachments should show attachments link", () => {
    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={true}
          hasCoverage={true}
        />
      </PinState>
    );

    expect(queryByTestId("nav-link-attachments")).not.toBeNull();
  });

  it("when no attachments should not show attachments link", () => {
    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={false}
          hasCoverage={true}
        />
      </PinState>
    );

    expect(queryByTestId("nav-link-attachments")).toBeNull();
  });

  it("when coverage should show coverage link", () => {
    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={true}
          hasCoverage={true}
        />
      </PinState>
    );

    expect(queryByTestId("nav-link-coverage")).not.toBeNull();
  });

  it("when no coverage should not show coverage link", () => {
    const { queryByTestId } = render(
      <PinState publicId={publicId}>
        <SideMenu
          publicId={publicId}
          testRunSummary={testRunSummary}
          hasAttachments={false}
          hasCoverage={false}
        />
      </PinState>
    );

    expect(queryByTestId("nav-link-coverage")).toBeNull();
  });
});
