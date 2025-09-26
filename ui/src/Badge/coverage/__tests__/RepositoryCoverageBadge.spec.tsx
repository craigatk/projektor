import "@testing-library/jest-dom";
import React from "react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../../service/AxiosService";
import { act, render } from "@testing-library/react";
import {
  createHistory,
  createMemorySource,
  LocationProvider,
} from "@reach/router";
import RepositoryCoverageBadge from "../RepositoryCoverageBadge";

jest.mock("../../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("RepositoryCoverageBadge", () => {
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
    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/badge/coverage`)
      .reply(200, "<span>my-badge</span>");

    document.execCommand = jest.fn();

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <RepositoryCoverageBadge repoName={repoName} />
      </LocationProvider>,
    );

    expect(await findByTestId("coverage-badge-contents")).toHaveTextContent(
      "my-badge",
    );

    expect(await findByTestId("coverage-badge-copy-link")).toHaveAttribute(
      "data-badge",
      "[![Code coverage percentage](undefined/repo/my-org/my-repo/badge/coverage)](undefined/repository/my-org/my-repo/coverage)",
    );

    await act(async () => {
      (await findByTestId("coverage-badge-copy-link")).click();
    });

    await findByTestId("coverage-badge-copied");

    expect(document.execCommand).toHaveBeenCalledWith("copy");
  });
});
