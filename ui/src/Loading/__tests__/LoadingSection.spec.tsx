import "@testing-library/jest-dom/extend-expect";
import React from "react";
import { render } from "@testing-library/react";
import LoadingSection from "../LoadingSection";
import LoadingState from "../LoadingState";

describe("LoadingSection", () => {
  it("should display progress bar when loading", () => {
    const { queryByTestId } = render(
      <LoadingSection
        loadingState={LoadingState.Loading}
        successComponent={
          <div data-testid="loading-section-success">Success!</div>
        }
      />
    );

    expect(queryByTestId("loading-section-progress")).not.toBeNull();
    expect(queryByTestId("loading-section-success")).toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should display success section when successful", () => {
    const { queryByTestId } = render(
      <LoadingSection
        loadingState={LoadingState.Success}
        successComponent={
          <div data-testid="loading-section-success">Success!</div>
        }
      />
    );

    expect(queryByTestId("loading-section-progress")).toBeNull();
    expect(queryByTestId("loading-section-success")).not.toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });

  it("should display error section when errored", () => {
    const { queryByTestId } = render(
      <LoadingSection
        loadingState={LoadingState.Error}
        successComponent={
          <div data-testid="loading-section-success">Success!</div>
        }
      />
    );

    expect(queryByTestId("loading-section-progress")).toBeNull();
    expect(queryByTestId("loading-section-success")).toBeNull();
    expect(queryByTestId("loading-section-error")).not.toBeNull();
  });

  it("should display custom error component when there is one passed in", () => {
    const { queryByTestId } = render(
      <LoadingSection
        loadingState={LoadingState.Error}
        successComponent={
          <div data-testid="loading-section-success">Success!</div>
        }
        errorComponent={<div data-testid="custom-error-section">Error!</div>}
      />
    );

    expect(queryByTestId("custom-error-section")).not.toBeNull();
    expect(queryByTestId("loading-section-progress")).toBeNull();
    expect(queryByTestId("loading-section-success")).toBeNull();
    expect(queryByTestId("loading-section-error")).toBeNull();
  });
});
