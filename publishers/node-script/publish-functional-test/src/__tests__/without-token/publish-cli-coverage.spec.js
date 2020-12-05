const { exec } = require("child_process");
const waitForExpect = require("wait-for-expect");
const { extractTestRunId } = require("../util/parse_output");
const {
  fetchTestRunSummary,
  fetchCoverage,
    fetchCoverageFiles,
} = require("../util/projektor_client");
const { verifyOutput } = require("../verify/cli_output_verify");

describe("Publishing with coverage via CLI", () => {
  const serverPort = "8082";

  it("should publish results and coverage to server configured in config file", async (done) => {
    exec(
      `yarn projektor-publish --configFile=src/__tests__/without-token/projektor-coverage.json`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();
        expect(stdout).not.toContain(
          "No test results files found in locations"
        );

        expect(stdout).toContain(
          "Sending 1 coverage result(s) to Projektor server"
        );
        expect(stdout).toContain("Finished sending coverage");

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        await waitForExpect(async () => {
          const testRunSummaryResponse = await fetchTestRunSummary(
            testRunId,
            serverPort
          );
          expect(testRunSummaryResponse.status).toEqual(200);
        });

        await waitForExpect(async () => {
          const coverageResponse = await fetchCoverage(testRunId, serverPort);
          expect(coverageResponse.status).toEqual(200);

          console.log("Coverage data", coverageResponse.data);

          expect(
            coverageResponse.data.overall_stats.line_stat.covered_percentage
          ).toBe(90.5);
        });

        done();
      }
    );
  });

  it("should publish results and coverage to server configured on command line", async (done) => {
    exec(
      `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --coverage=coverage/*.xml results/*.xml`,
      async (error, stdout, stderr) => {
        verifyOutput(error, stdout, stderr, serverPort);
        expect(error).toBeNull();

        expect(stdout).toContain(
          "Sending 1 coverage result(s) to Projektor server"
        );
        expect(stdout).toContain("Finished sending coverage");

        const testRunId = extractTestRunId(stdout);
        console.log("Test ID", testRunId);

        await waitForExpect(async () => {
          const testRunSummaryResponse = await fetchTestRunSummary(
            testRunId,
            serverPort
          );
          expect(testRunSummaryResponse.status).toEqual(200);
        });

        await waitForExpect(async () => {
          const coverageResponse = await fetchCoverage(testRunId, serverPort);
          expect(coverageResponse.status).toEqual(200);

          console.log("Coverage data", coverageResponse.data);

          expect(
            coverageResponse.data.overall_stats.line_stat.covered_percentage
          ).toBe(90.5);
        });

        done();
      }
    );
  });

    it("should publish coverage with base directory path to server configured on command line", async (done) => {
        exec(
            `yarn projektor-publish --serverUrl=http://localhost:${serverPort} --coverage=coverage/*.xml --baseDirectoryPath=ui results/*.xml`,
            async (error, stdout, stderr) => {
                verifyOutput(error, stdout, stderr, serverPort);
                expect(error).toBeNull();

                expect(stdout).toContain(
                    "Sending 1 coverage result(s) to Projektor server"
                );
                expect(stdout).toContain("Finished sending coverage");

                const testRunId = extractTestRunId(stdout);
                console.log("Test ID", testRunId);

                await waitForExpect(async () => {
                    const coverageResponse = await fetchCoverage(testRunId, serverPort);
                    expect(coverageResponse.status).toEqual(200);
                });


                const coverageFilesResponse = await fetchCoverageFiles(testRunId, "All files", serverPort)
                expect(coverageFilesResponse.status).toEqual(200);

                const coverageFilesResponseData = coverageFilesResponse.data
                const overallCoverageGraphsFile = coverageFilesResponseData.files.find(file => file.file_name === 'OverallCoverageGraphs.tsx')
                expect(overallCoverageGraphsFile).toBeDefined()

                expect(overallCoverageGraphsFile.directory_name).toBe("src/Coverage")
                expect(overallCoverageGraphsFile.file_path).toBe("ui/src/Coverage/OverallCoverageGraphs.tsx")

                done();
            }
        );
    });
});
