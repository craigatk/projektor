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
import RepositoryTestsBadge from "../RepositoryTestsBadge";

jest.mock("../../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("RepositoryTestsBadge", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should display tests badge", async () => {
    const repoName = "my-org/my-repo";
    mockAxios
      .onGet(`http://localhost:8080/repo/${repoName}/badge/tests`)
      .reply(200, "<span>my-badge</span>");

    document.execCommand = jest.fn();

    const { findByTestId } = render(
      <LocationProvider history={createHistory(createMemorySource("/ui"))}>
        <RepositoryTestsBadge repoName={repoName} />
      </LocationProvider>,
    );

    expect(await findByTestId("tests-badge-contents")).toHaveTextContent(
      "my-badge",
    );

    expect(await findByTestId("tests-badge-copy-link")).toHaveAttribute(
      "data-badge",
      "[![Test results](undefined/repo/my-org/my-repo/badge/tests)](undefined/repo/my-org/my-repo/run/latest)",
    );

    await act(async () => {
      (await findByTestId("tests-badge-copy-link")).click();
    });

    await findByTestId("tests-badge-copied");

    expect(document.execCommand).toHaveBeenCalledWith("copy");
  });
});
