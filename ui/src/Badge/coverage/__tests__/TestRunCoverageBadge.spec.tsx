import "@testing-library/jest-dom/extend-expect";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import { render } from "@testing-library/react";
import {
  createHistory,
  createMemorySource,
  LocationProvider,
} from "@reach/router";
import TestRunCoverageBadge from "../TestRunCoverageBadge";

describe("TestRunCoverageBadge", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should display coverage badge", async () => {
    const repoName = "my-org/my-repo";
    const publicId = "12345";
    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/badge/coverage`)
      .reply(200, "<span>my-badge</span>");

    document.execCommand = jest.fn();

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <TestRunCoverageBadge publicId={publicId} repoName={repoName} />
      </LocationProvider>,
    );

    expect(await findByTestId("coverage-badge-contents")).toHaveTextContent(
      "my-badge",
    );

    expect(await findByTestId("coverage-badge-copy-link")).toHaveAttribute(
      "data-badge",
      "[![Code coverage percentage](undefined/repo/my-org/my-repo/badge/coverage)](undefined/repository/my-org/my-repo/coverage)",
    );

    (await findByTestId("coverage-badge-copy-link")).click();

    await findByTestId("coverage-badge-copied");

    expect(document.execCommand).toHaveBeenCalledWith("copy");
  });
});
