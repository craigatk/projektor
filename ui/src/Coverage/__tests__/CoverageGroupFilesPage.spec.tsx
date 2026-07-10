import "@testing-library/jest-dom";
import React from "react";
import { render } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";
import { axiosInstance } from "../../service/AxiosService";
import { CoverageFiles, TestRunGitMetadata } from "../../model/TestRunModel";
import { createCoverageStats } from "../../testUtils/coverageTestUtils";
import CoverageGroupFilesPage from "../CoverageGroupFilesPage";

vi.mock("../../service/EnvService", () => ({
  baseUrl: (): string => "http://localhost:8080/",
}));

describe("CoverageGroupFilesPage", () => {
  let mockAxios;

  beforeEach(() => {
    // @ts-ignore
    mockAxios = new MockAdapter(axiosInstance);
  });

  afterEach(() => {
    mockAxios.restore();
  });

  it("should fetch and render coverage files for the group", async () => {
    const publicId = "TESTRUN1";
    const coverageGroupName = "unit";

    const coverageFiles = {
      files: [
        {
          fileName: "MyFile.java",
          directoryName: "com.example",
          stats: createCoverageStats(85),
          missedLines: [],
          partialLines: [],
        },
      ],
    } as CoverageFiles;

    const gitMetadata = { repoName: "my-repo" } as TestRunGitMetadata;

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/coverage/${coverageGroupName}/files`,
      )
      .reply(200, coverageFiles);

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/metadata/git`)
      .reply(200, gitMetadata);

    const { findByTestId } = render(
      <CoverageGroupFilesPage
        publicId={publicId}
        coverageGroupName={coverageGroupName}
      />,
    );

    expect(await findByTestId("coverage-group-files-title")).toHaveTextContent(
      `Coverage for files in ${coverageGroupName}`,
    );
    expect(await findByTestId("coverage-file-name-1")).toBeInTheDocument();
  });

  it("should render an error message when the coverage files fetch fails", async () => {
    const publicId = "TESTRUN2";
    const coverageGroupName = "unit";

    mockAxios
      .onGet(
        `http://localhost:8080/run/${publicId}/coverage/${coverageGroupName}/files`,
      )
      .reply(500);

    mockAxios
      .onGet(`http://localhost:8080/run/${publicId}/metadata/git`)
      .reply(200, {});

    const { findByTestId } = render(
      <CoverageGroupFilesPage
        publicId={publicId}
        coverageGroupName={coverageGroupName}
      />,
    );

    expect(await findByTestId("loading-section-error")).toBeInTheDocument();
  });
});
